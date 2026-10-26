package com.luoshitu.video

import com.luoshitu.video.data.config.PathResolver
import org.junit.Assert.assertEquals
import org.junit.Test

/** 3.5 相对路径解析（S1 DoD 单测项） */
class PathResolverTest {

    @Test fun `absolute url passes through`() {
        assertEquals("https://a.com/x/y.json", PathResolver.resolve("http://cfg.com/config.json", "https://a.com/x/y.json"))
    }

    @Test fun `sibling path resolves against config directory`() {
        assertEquals("http://cfg.com/jar/x.jar", PathResolver.resolve("http://cfg.com/config.json", "./jar/x.jar"))
    }

    @Test fun `bare relative path equals dot-slash`() {
        assertEquals("http://cfg.com/jar/x.jar", PathResolver.resolve("http://cfg.com/config.json", "jar/x.jar"))
    }

    @Test fun `dot-dot climbs one level`() {
        assertEquals("http://cfg.com/lib/y.js", PathResolver.resolve("http://cfg.com/sub/config.json", "../lib/y.js"))
    }

    @Test fun `dot-dot climbs above root is clamped`() {
        assertEquals("http://cfg.com/y.js", PathResolver.resolve("http://cfg.com/sub/config.json", "../../y.js"))
    }

    @Test fun `base without path`() {
        assertEquals("http://cfg.com/x.json", PathResolver.resolve("http://cfg.com", "./x.json"))
    }

    @Test fun `base with port and nested dir`() {
        assertEquals("http://cfg.com:8080/a/b.js", PathResolver.resolve("http://cfg.com:8080/a/c/config.json", "./b.js"))
    }

    @Test fun `absolute path anchors to host`() {
        assertEquals("http://cfg.com/abs/z.js", PathResolver.resolve("http://cfg.com/sub/config.json", "/abs/z.js"))
    }

    @Test fun `query on base is dropped for referenced file`() {
        assertEquals("http://cfg.com/jar/x.jar", PathResolver.resolve("http://cfg.com/config.json?token=1", "./jar/x.jar"))
    }

    @Test fun `empty or null returns base`() {
        assertEquals("http://cfg.com/config.json", PathResolver.resolve("http://cfg.com/config.json", null))
        assertEquals("http://cfg.com/config.json", PathResolver.resolve("http://cfg.com/config.json", "  "))
    }

    @Test fun `dot-dot to directory only`() {
        assertEquals("http://cfg.com/sub/config.json", PathResolver.resolve("http://cfg.com/sub/config.json", "../sub/"))
    }
}
