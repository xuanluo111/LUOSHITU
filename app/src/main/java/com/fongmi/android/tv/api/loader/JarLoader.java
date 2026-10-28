package com.fongmi.android.tv.api.loader;

import android.content.Context;
import android.util.Log;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.utils.UrlUtil;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;
import com.github.catvod.net.Download;
import com.github.catvod.net.Net;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Crypto;
import com.github.catvod.utils.Local;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.RuntimeModule;
import com.github.catvod.utils.Task;

import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class JarLoader {

    private static final String TAG = JarLoader.class.getSimpleName();
    /**
     * 洛视频：spider.jar 常挂在极慢链路上（实测 1.1MB 只下到 2% 就永久 stall，字节数几十秒不动），
     * 而首页是同步等 parseJar 结果的。没有总时长预算，就等于首页永久空转且不报任何错。
     */
    private static final long JAR_DOWNLOAD_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(45);
    /** 下载失败后的一段时间内不再重试，否则每次进首页都要再干等一轮。 */
    private static final long JAR_RETRY_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10);

    private final ConcurrentHashMap<String, DexClassLoader> loaders;
    private final ConcurrentHashMap<String, Method> methods;
    private final ConcurrentHashMap<String, Spider> spiders;
    private final ConcurrentHashMap<String, Object> locks;
    private final ConcurrentHashMap<String, Long> failures;
    /** 同一 key 同一时间只跑一个后台刷新。 */
    private final Set<String> refreshing;
    private volatile String recent;

    public JarLoader() {
        loaders = new ConcurrentHashMap<>();
        methods = new ConcurrentHashMap<>();
        spiders = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();
        failures = new ConcurrentHashMap<>();
        refreshing = ConcurrentHashMap.newKeySet();
    }

    public void clear() {
        Throwable failure = destroySpiders();
        loaders.clear();
        methods.clear();
        spiders.clear();
        locks.clear();
        failures.clear();
        refreshing.clear();
        recent = null;
        if (failure instanceof RuntimeException error) throw error;
        if (failure instanceof Error error) throw error;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    private Throwable destroySpiders() {
        Throwable failure = null;
        for (Spider spider : spiders.values()) failure = destroy(spider, failure);
        return failure;
    }

    private Throwable destroy(Spider spider, Throwable failure) {
        try {
            destroy(spider);
        } catch (RuntimeException | Error error) {
            if (failure == null) failure = error;
            else if (failure != error) failure.addSuppressed(error);
        }
        return failure;
    }

    private void destroy(Spider spider) {
        spider.dialog.close();
        Throwable failure = destroySpider(spider);
        failure = closeNetwork(spider, failure);
        if (failure instanceof RuntimeException error) throw error;
        if (failure instanceof Error error) throw error;
    }

    private Throwable destroySpider(Spider spider) {
        try {
            spider.destroy();
            return null;
        } catch (RuntimeException | Error error) {
            return error;
        }
    }

    private Throwable closeNetwork(Spider spider, Throwable failure) {
        spider.js.close();
        try {
            if (spider.net != null) spider.net.close();
        } catch (RuntimeException | Error error) {
            if (failure == null) failure = error;
            else if (failure != error) failure.addSuppressed(error);
        }
        return failure;
    }

    private void load(String key, File file) {
        if (file == null) return;
        if (Thread.interrupted()) return;
        if (!Path.exists(file) || !file.setReadOnly()) return;
        String cachePath = Path.jar().getAbsolutePath();
        DexClassLoader loader = new DexClassLoader(file.getAbsolutePath(), cachePath, cachePath, RuntimeModule.getSpiderClassLoader());
        invokeInit(loader);
        invokeProxy(key, loader);
        loaders.put(key, loader);
    }

    private void invokeInit(DexClassLoader loader) {
        try {
            Class<?> clz = loader.loadClass("com.github.catvod.spider.Init");
            Method method = clz.getMethod("init", Context.class);
            method.invoke(clz, App.get());
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable e) {
            RuntimeModule.MissingException missing = RuntimeModule.MissingException.find(e);
            if (missing != null) throw missing;
            e.printStackTrace();
        }
    }

    private void invokeProxy(String key, DexClassLoader loader) {
        try {
            Class<?> clz = loader.loadClass("com.github.catvod.spider.Proxy");
            Method method = clz.getMethod("proxy", Map.class);
            methods.put(key, method);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void parseJar(String key, String jar) {
        if (loaders.containsKey(key)) return;
        if (jar.startsWith("assets")) jar = UrlUtil.convert(jar);
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            if (loaders.containsKey(key)) return;
            String[] texts = jar.split(";md5;");
            String md5 = texts.length > 1 ? texts[1].trim() : "";
            if (md5.startsWith("http")) md5 = OkHttp.string(md5).trim();
            jar = texts[0];
            if (!md5.isEmpty() && Crypto.equals(Path.jar(jar), md5)) {
                load(key, Path.jar(jar));
                return;
            }
            File cached = jar.startsWith("http") ? Path.jar(jar) : null;
            // 洛视频：配置里不写 ;md5; 是很常见的写法，而原逻辑此时每次都整包重下。
            // 该源实测 80KB/s，1.1MB 要十几到几十秒，慢一点就超时 → 首页永久空转。
            // 盘上已有可用缓存就先用起来（首页立即可进），新版本交给后台刷新。
            // 缓存损坏（截断的下载曾经落盘）时 DexClassLoader 构造会抛，删掉坏文件走下载路径自愈。
            if (cached != null && md5.isEmpty() && cached.isFile() && cached.length() > 0) {
                try {
                    load(key, cached);
                    refresh(key, jar, cached);
                    return;
                } catch (RuntimeModule.MissingException e) {
                    throw e;
                } catch (Throwable e) {
                    cached.delete();
                    loaders.remove(key);
                    failures.put(key, System.currentTimeMillis());
                    Log.w(TAG, "jar cache corrupted, will redownload: " + jar, e);
                }
            }
            Long failedAt = failures.get(key);
            if (failedAt != null && System.currentTimeMillis() - failedAt < JAR_RETRY_INTERVAL_MS) return;
            if (jar.startsWith("http")) load(key, download(key, jar));
            else if (jar.startsWith("file")) load(key, Path.local(jar));
        }
    }

    /**
     * 洛视频：后台刷新 jar 缓存。先下到独立的 .part 文件再 rename 覆盖——
     * rename 是原子替换，旧 inode 仍被已建好的 DexClassLoader 持有，不会写坏正在运行的站点。
     */
    private void refresh(String key, String jar, File cached) {
        if (!refreshing.add(key)) return;
        // 洛视频：后台刷新不能和用户抢带宽。这个源实测只有十几 KB/s，1.1MB 的 jar 要下 80 多秒，
        // 立即开刷会把首页/分类请求拖到 TIMEOUT_VOD(30s) 超时，表现为首页空白。
        // 延后 60s 再跑；Task.schedule 用的是独立 scheduler，不占 executor。
        Task.schedule(() -> Task.submitLarge(() -> doRefresh(key, jar, cached)), 60, TimeUnit.SECONDS);
    }

    /**
     * 洛视频：先下到独立的 .part 文件再 rename 覆盖——
     * rename 是原子替换，旧 inode 仍被已建好的 DexClassLoader 持有，不会写坏正在运行的站点。
     */
    private void doRefresh(String key, String jar, File cached) {
        File temp = new File(cached.getParentFile(), cached.getName() + ".part");
        try {
            Download.create(jar, temp).get();
            long size = temp.length();
            // 洛视频：服务器不带 Content-Length 且提前断流时，截断内容会被 publish 成目标文件。
            // rename 前做最小魔数校验（zip/jar 的 PK 或裸 dex），坏文件不留盘，保留旧缓存继续可用。
            if (size <= 0 || size == cached.length() || !looksLikeArchive(temp)) {
                temp.delete();
                return;
            }
            if (temp.renameTo(cached)) {
                failures.remove(key);
                Log.i(TAG, "jar refreshed: " + jar + " " + size);
            } else temp.delete();
        } catch (Throwable e) {
            temp.delete();
            Log.w(TAG, "jar refresh skipped: " + jar, e);
        } finally {
            refreshing.remove(key);
        }
    }

    private boolean looksLikeArchive(File file) {
        try (InputStream in = new FileInputStream(file)) {
            byte[] head = new byte[3];
            if (in.read(head) != head.length) return false;
            if (head[0] == 'P' && head[1] == 'K') return true;
            return head[0] == 'd' && head[1] == 'e' && head[2] == 'x';
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 洛视频：jar 下载必须有终点。基座 Download 的 120s 只作用于“单次 socket 读”，
     * 服务器持续缓慢吐字节时永远触发不到；而 initSite() 是同步等这里的，
     * 一等不到整个首页就永久空转。这里再加一层总时长预算，超时返回 null 由 load() 跳过。
     */
    private File download(String key, String jar) {
        try {
            File file = Task.submitLarge(() -> Download.create(jar, Path.jar(jar)).get(), JAR_DOWNLOAD_TIMEOUT_MS).get();
            failures.remove(key);
            return file;
        } catch (InterruptedException | CancellationException e) {
            // 洛视频：用户切配置触发的取消不是下载失败，不能计入退避，否则 10 分钟内切回该配置站点全空且无提示。
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            Log.i(TAG, "jar download cancelled: " + jar);
            return null;
        } catch (Throwable e) {
            failures.put(key, System.currentTimeMillis());
            Log.e(TAG, "jar download give up: " + jar, e);
            return null;
        }
    }

    public DexClassLoader dex(String jar) {
        try {
            String jaKey = Crypto.md5(jar);
            parseJar(jaKey, jar);
            return loaders.get(jaKey);
        } catch (Throwable e) {
            RuntimeModule.MissingException missing = RuntimeModule.MissingException.find(e);
            if (missing != null) throw missing;
            e.printStackTrace();
            return null;
        }
    }

    public Spider getSpider(String key, String api, String ext, String jar) {
        String jaKey = Crypto.md5(jar);
        String spKey = jaKey + key;
        return spiders.computeIfAbsent(spKey, k -> {
            try {
                parseJar(jaKey, jar);
                DexClassLoader loader = loaders.get(jaKey);
                if (loader == null) return new SpiderNull();
                Spider spider = (Spider) loader.loadClass("com.github.catvod.spider." + api.split("csp_")[1]).newInstance();
                return initSpider(spider, key, ext);
            } catch (Throwable e) {
                RuntimeModule.MissingException missing = RuntimeModule.MissingException.find(e);
                if (missing != null) throw missing;
                e.printStackTrace();
                return new SpiderNull();
            }
        });
    }

    private Spider initSpider(Spider spider, String key, String ext) throws Exception {
        spider.siteKey = key;
        spider.net = new Net(JsHost.INSTANCE, key);
        spider.local = new Local(key);
        try {
            spider.init(App.get(), ext);
            return spider;
        } catch (Throwable e) {
            spider.dialog.close();
            spider.js.close();
            try {
                spider.net.close();
            } catch (RuntimeException | Error error) {
                if (error != e) e.addSuppressed(error);
            }
            throw e;
        }
    }

    private static DexClassLoader openDex(File file) throws Exception {
        if (!Path.exists(file)) throw new IllegalArgumentException("JAR is missing or empty: " + file);
        if (!file.setReadOnly()) throw new IllegalStateException("Cannot mark JAR read-only: " + file);
        String cachePath = file.getParent();
        return new DexClassLoader(file.getAbsolutePath(), cachePath, cachePath, RuntimeModule.getSpiderClassLoader());
    }

    @SuppressWarnings("deprecation")
    static List<String> spiders(File file) throws Exception {
        DexClassLoader loader = openDex(file);
        TreeSet<String> names = new TreeSet<>();
        DexFile dex = new DexFile(file);
        try (Closeable close = dex::close) {
            readSpiders(dex, loader, names);
        }
        return new ArrayList<>(names);
    }

    private static void readSpiders(DexFile dex, DexClassLoader loader, TreeSet<String> names) throws Exception {
        Enumeration<String> entries = dex.entries();
        while (entries.hasMoreElements()) {
            if (Thread.currentThread().isInterrupted()) throw new CancellationException("Crawler test session has ended");
            addSpider(entries.nextElement().replace('/', '.'), loader, names);
        }
    }

    private static void addSpider(String name, DexClassLoader loader, TreeSet<String> names) throws Exception {
        String prefix = "com.github.catvod.spider.";
        if (!name.startsWith(prefix)) return;
        Class<?> clz = Class.forName(name, false, loader);
        if (clz.getClassLoader() != loader || !Spider.class.isAssignableFrom(clz) || !Modifier.isPublic(clz.getModifiers()) || Modifier.isAbstract(clz.getModifiers())) return;
        if (Arrays.stream(clz.getConstructors()).anyMatch(constructor -> constructor.getParameterTypes().length == 0)) names.add("csp_" + name.substring(prefix.length()));
    }

    static DexClassLoader newDex(File file) throws Exception {
        DexClassLoader loader = openDex(file);
        try {
            Class<?> clz = loader.loadClass("com.github.catvod.spider.Init");
            clz.getMethod("init", Context.class).invoke(null, App.get());
        } catch (ClassNotFoundException ignored) {
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception error) throw error;
            if (e.getCause() instanceof Error error) throw error;
            throw e;
        }
        return loader;
    }

    static Spider newSpider(String key, String api, DexClassLoader loader) throws Exception {
        Spider spider = (Spider) loader.loadClass("com.github.catvod.spider." + api.substring(4)).getDeclaredConstructor().newInstance();
        spider.siteKey = key;
        spider.net = new Net(JsHost.INSTANCE, key);
        spider.local = new Local(key);
        return spider;
    }

    private DexClassLoader requireRecentLoader() {
        DexClassLoader loader = loaders.get(recent);
        if (loader == null) throw new IllegalStateException("No jar loaded for recent key: " + recent);
        return loader;
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) throws Throwable {
        Class<?> clz = requireRecentLoader().loadClass("com.github.catvod.parser.Json" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class);
        return (JSONObject) method.invoke(null, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) throws Throwable {
        Class<?> clz = requireRecentLoader().loadClass("com.github.catvod.parser.Mix" + key);
        Method method = clz.getMethod("parse", LinkedHashMap.class, String.class, String.class, String.class);
        return (JSONObject) method.invoke(null, jxs, name, flag, url);
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        Method method = recent != null ? methods.get(recent) : null;
        Object[] result = proxyInvoke(method, params);
        if (result != null) return result;
        return tryOthers(params);
    }

    private Object[] tryOthers(Map<String, String> p) {
        return methods.entrySet().stream().filter(e -> !e.getKey().equals(recent)).map(e -> proxyInvoke(e.getValue(), p)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private Object[] proxyInvoke(Method method, Map<String, String> params) {
        try {
            return method == null ? null : (Object[]) method.invoke(null, params);
        } catch (Throwable e) {
            RuntimeModule.MissingException missing = RuntimeModule.MissingException.find(e);
            if (missing != null) throw missing;
            e.printStackTrace();
            return null;
        }
    }
}
