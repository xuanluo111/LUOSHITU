#!/usr/bin/env python3
# 解析 gradle javac 输出，自动生成/修正 androidx.media3 补齐桩（O2 决策，见 docs/S0-立项报告.md §9）
import re, sys, os, json

LOG = sys.argv[1]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STUB = os.path.join(ROOT, 'app/src/main/java')
STATE = os.path.join(ROOT, 'build/stubstate.json')

def load_state():
    raw = json.load(open(STATE)) if os.path.exists(STATE) else {}
    return {k: v for k, v in raw.items() if k.startswith('androidx.media3.')}

def save_state(s):
    os.makedirs(os.path.dirname(STATE), exist_ok=True)
    json.dump(s, open(STATE, 'w'), ensure_ascii=False, indent=1)

def clean_params(params):
    params = params.strip()
    while params.startswith('((') and params.endswith('))'): params = params[1:-1].strip()
    if params.startswith('(') and params.endswith(')'): params = params[1:-1].strip()
    elif params in ('()', '') or params == '()': return ''
    out = []
    depth = 0; cur = ''
    for ch in (params + ','):
        if ch == '<': depth += 1
        if ch == '>': depth -= 1
        if ch == ',' and depth == 0:
            out.append(cur.strip()); cur = ''
        else: cur += ch
    fixed = []
    for a in out:
        a = a.strip().replace('<空值>', 'Object').replace('<null>', 'Object')
        if not a or '[' in a or '<' in a or '(' in a: a = 'Object'
        fixed.append(a)
    return ', '.join(a for a in fixed if a)

def default_ret(name):
    if re.match(r'^(is|has|can|should|need|supports|contains|matches|equals)', name): return 'boolean'
    if re.match(r'^(set|add|remove|clear|reset|prepare|apply|enable|disable|release|stop|start|destroy|close|refresh|reload|update|init|on[A-Z])', name): return 'void'
    return 'Object'

def named(params):
    if not params.strip(): return ''
    out = []
    for i, a in enumerate([a.strip() for a in params.split(',') if a.strip()]):
        out.append(f'{a} p{i}')
    return ', '.join(out)

def gen_file(pkg, cls, info):
    if not (pkg == 'androidx.media3' or pkg.startswith('androidx.media3.')):
        return  # 硬防护：禁止写入 androidx.media3 以外的任何路径
    path = os.path.join(STUB, *pkg.split('.'), cls + '.java')
    path = os.path.join(STUB, *pkg.split('.'), cls + '.java')
    if os.path.exists(path) and ('@hand' in open(path, encoding='utf-8', errors='replace').read(2048) or '手工维护' in open(path, encoding='utf-8', errors='replace').read(2048)):
        return  # 手工维护的桩不覆盖
    os.makedirs(os.path.dirname(path), exist_ok=True)
    body = []
    for c in info.get('ctors', []): body.append('\t' + c + ' { }\n')
    for f in info.get('fields', []): body.append(f'\tpublic Object {f};\n')
    for m in info.get('methods', []):
        ret, name, params = m['ret'], m['name'], m['params']
        ps = named(params)
        if ret == 'void': body.append(f'\tpublic void {name}({ps}) {{ }}\n')
        elif ret == 'boolean': body.append(f'\tpublic boolean {name}({ps}) {{ return false; }}\n')
        elif ret == 'int': body.append(f'\tpublic int {name}({ps}) {{ return 0; }}\n')
        elif ret == 'long': body.append(f'\tpublic long {name}({ps}) {{ return 0L; }}\n')
        else: body.append(f'\tpublic {ret} {name}({ps}) {{ return null; }}\n')
    header = '// 洛视频补齐桩：替代上游私有 aar 中的 media3 fork 类（docs/S0-立项报告.md 第 9 节）。自动生成。\n'
    old_imports = []
    if os.path.exists(path):
        try:
            for l in open(path, encoding='utf-8'):
                if l.startswith('import '): old_imports.append(l)
        except Exception: pass
    content = header + f'package {pkg};\n\n' + ''.join(old_imports) + '\npublic class {cls} {{\n'.format(cls=cls) + ''.join(body)
    for n in info.get('nested', []): content += f'\tpublic static class {n} {{ }}\n'
    content += '}\n'
    open(path, 'w').write(content)

OFFICIAL = {'R', 'PlayerView', 'MatroskaExtractor', 'DefaultExtractorsFactory', 'SubtitleView', 'CaptionStyleCompat', 'TimeBar', 'AspectRatioFrameLayout', 'MediaItem', 'Player', 'ExoPlayer'}

def main():
    text = open(LOG, encoding='utf-8', errors='replace').read()
    state = load_state()
    pkgs = set(re.findall(r'程序包 ?([\w.]+)不存在', text)) | set(re.findall(r'package ([\w.]+) does not exist', text))
    src_import_cache = {}
    def imports_of(path):
        if path not in src_import_cache:
            d = {}
            try:
                for im in open(path, encoding='utf-8', errors='replace'):
                    m = re.match(r'import (androidx\.media3[\w.]*)\.(\w+);', im.strip())
                    if m: d.setdefault(m.group(2), m.group(1))
                for im in open(path, encoding='utf-8', errors='replace'):
                    m = re.match(r'import (androidx\.media3[\w.]*)\.\*;', im.strip())
                    if m: d.setdefault('*', []).append(m.group(1))
            except FileNotFoundError: pass
            src_import_cache[path] = d
        return src_import_cache[path]

    stub_names = {}
    for dirpath, _, files in os.walk(STUB):
        for fn in files:
            if fn.endswith('.java'):
                rel = os.path.relpath(os.path.join(dirpath, fn), STUB)
                parts = rel[:-5].split(os.sep)
                fqn = '.'.join(parts[:-1]) + '.' + parts[-1]
                stub_names[parts[-1]] = fqn
                stub_names[fqn] = fqn
    unresolved = {}

    blocks = re.findall(r'([^\s:]+\.java):(\d+): (?:错误|error): 找不到符号\n(?:.*\n)*?\s*符号: +(\S+) ?([^\n]*)\n\s*位置: ([^\n]*)', text)
    for f, line, kind, detail, loc in blocks:
        if kind in ('类', 'class'):
            name = detail.strip()
            imps = imports_of(f.split(':')[0])
            fqn = None
            if name in imps:
                fqn = imps[name] + '.' + name
            else:
                for wpkg in imps.get('*', []):
                    if wpkg in pkgs: fqn = wpkg + '.' + name; break
                if not fqn and stub_names.get(name) is None and loc:
                    lm2 = re.search(r'类 ([\w.]+)|class ([\w.]+)', loc)
                    parent = (lm2.group(1) or lm2.group(2)) if lm2 else None
                    pfqn = stub_names.get(parent) if parent else None
                    if pfqn:
                        info = state.setdefault(pfqn, {'pkg': pfqn.rsplit('.',1)[0], 'name': pfqn.rsplit('.',1)[1], 'methods': [], 'fields': [], 'ctors': [], 'nested': []})
                        if name not in info.setdefault('nested', []): info['nested'].append(name)
                        continue
            if fqn:
                pkg, cls = fqn.rsplit('.', 1)
                state.setdefault(fqn, {'pkg': pkg, 'name': cls, 'methods': [], 'fields': [], 'ctors': [], 'nested': []})
        elif kind in ('方法', 'method'):
            name = detail.strip().split('(')[0]
            params = detail.strip()[len(name):].strip()
            lm = re.search(r'类 ([\w.]+)|class ([\w.]+)|类型为 ?([\w.<>]*)的(?:局部)?变量 ([\w.]+)', loc)
            tgt = (lm.group(1) or lm.group(2) or lm.group(3)) if lm else None
            fqn = stub_names.get(tgt) if tgt else None
            if fqn and fqn.rsplit('.',1)[-1] in OFFICIAL: fqn = None
            if fqn:
                pkg, cls = fqn.rsplit('.', 1)
                info = state.setdefault(fqn, {'pkg': pkg, 'name': cls, 'methods': [], 'fields': [], 'ctors': [], 'nested': []})
                params = clean_params(params)
                if not any(m['name'] == name and m['params'] == params for m in info['methods']):
                    info['methods'].append({'name': name, 'params': params, 'ret': default_ret(name)})
            elif tgt:
                unresolved.setdefault(tgt, set()).add(name + params)
        elif kind in ('变量', 'variable'):
            lm = re.search(r'类 ([\w.]+)|class ([\w.]+)|类型为 ?([\w.<>]*)的(?:局部|记录组件)? ?变量 ([\w.]+)|记录组件 ([\w.]+)', loc)
            tgt = (lm.group(1) or lm.group(2) or lm.group(3) or lm.group(5)) if lm else None
            fqn = stub_names.get(tgt) if tgt else None
            if fqn:
                pkg, cls = fqn.rsplit('.', 1)
                info = state.setdefault(fqn, {'pkg': pkg, 'name': cls, 'methods': [], 'fields': [], 'ctors': [], 'nested': []})
                if detail.strip() not in info['fields']: info['fields'].append(detail.strip())
            elif tgt:
                unresolved.setdefault(tgt, set()).add('field ' + detail.strip())
        elif kind in ('构造器', 'constructor'):
            lm = re.search(r'类 ([\w.]+)|class ([\w.]+)', loc)
            tgt = (lm.group(1) or lm.group(2)) if lm else None
            fqn = stub_names.get(tgt) if tgt else None
            if fqn:
                pkg, cls = fqn.rsplit('.', 1)
                info = state.setdefault(fqn, {'pkg': pkg, 'name': cls, 'methods': [], 'fields': [], 'ctors': [], 'nested': []})
                sig = 'public ' + cls + detail.strip()
                if sig not in info['ctors']: info['ctors'].append(sig)
            elif tgt:
                unresolved.setdefault(tgt, set()).add('ctor ' + detail.strip())

    # 返回类型修正：从"不兼容的类型: Object无法转换为 X"与 '==' 操作数错误反推
    import re as _re
    text_lines = text.split('\n')
    def ret_fix(match_type, fpath, lineno):
        try: srcline = text_lines[int(lineno) - 1] if fpath.startswith('/') else ''
        except Exception: return None
        idx = srcline.find(match_type)  # 定位失败则全行找
        return srcline
    for m in _re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 不兼容的类型: ?Object无法转换为 ?([\w.<>\[\], ]+)', text):
        fpath, lineno, want = m.group(1), m.group(2), m.group(3).strip()
        try: srcline = open(fpath.split(':')[0], encoding='utf-8', errors='replace').read().split('\n')[int(lineno) - 1]
        except Exception: continue
        for key, info in state.items():
            for meth in info.get('methods', []):
                if meth['ret'] == 'Object' and meth['name'] in srcline:
                    meth['ret'] = want
    for m in _re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 二元运算符 [^\n]*?第二个类型: ([\w.<>\[\], ]+)', text):
        fpath, lineno, want = m.group(1), m.group(2), m.group(3).strip()
        try: srcline = open(fpath.split(':')[0], encoding='utf-8', errors='replace').read().split('\n')[int(lineno) - 1]
        except Exception: continue
        for key, info in state.items():
            for meth in info.get('methods', []):
                if meth['ret'] == 'Object' and meth['name'] in srcline:
                    meth['ret'] = want

    try: seeds = json.load(open(os.path.join(ROOT, 'build/classmap.json')))
    except Exception: seeds = {}
    # Pass: 桩文件补 import（符号为类且错误文件是桩）
    imp_map = {}
    for base in ('app/src', 'catvod/src'):
        for dirpath, _, files in os.walk(os.path.join(ROOT, base)):
            for fn in files:
                if fn.endswith('.java'):
                    try:
                        for im in open(os.path.join(dirpath, fn), encoding='utf-8', errors='replace'):
                            m2 = re.match(r'import ([\w.]+)\.(\w+);', im.strip())
                            if m2 and not m2.group(1).startswith('java.lang'):
                                imp_map.setdefault(m2.group(2), m2.group(1))
                    except Exception: pass
    for k2, v2 in seeds.items(): imp_map.setdefault(k2, v2)
    def fix_import(fpath, fqn, simple):
        if not fqn.rsplit('.', 1)[-1][:1].isupper(): return  # 禁止插入包名 import
        try: content = open(fpath, encoding='utf-8').read()
        except Exception: return
        if 'import ' + fqn + ';' in content: return
        lines = content.split('\n')
        for i, l in enumerate(lines):
            if l.startswith('package '):
                lines.insert(i + 1, '\nimport ' + fqn + ';')
                open(fpath, 'w', encoding='utf-8').write('\n'.join(lines))
                return
    for m in _re.finditer(r'([^\s:]+\.java):\d+: (?:错误|error): 找不到符号\n(?:.*\n)*?\s*符号: +类 ([\w.]+)\n\s*位置: +类 ([\w.]+)', text):
        fpath, cls_name = m.group(1), m.group(2)
        simple = cls_name.rsplit('.', 1)[-1]
        fqn = imp_map.get(simple)
        if fqn and '.' in fqn:
            fix_import(fpath.split(':')[0], fqn, simple)

    # Pass: 参数类型按调用点修正（无法将类 X 中的方法 y 应用到给定类型 → 需要/发现）
    for m in _re.finditer(r'([^\s:]+\.java):\d+: (?:错误|error): 无法将类 ([\w.]+)中的方法 (\w+)应用到给定类型\n(?:.*\n)*?\s*发现: +([^\n]+)', text):
        tgt, mname, found = m.group(2), m.group(3), m.group(4).strip()
        fqn = stub_names.get(tgt)
        if not fqn: continue
        info = state.get(fqn)
        if not info: continue
        newp = clean_params('(' + found + ')')
        cands = [mm for mm in info['methods'] if mm['name'] == mname]
        if cands:
            cands[0]['params'] = newp
            for extra in cands[1:]: info['methods'].remove(extra)
        else:
            info['methods'].append({'name': mname, 'params': newp, 'ret': default_ret(mname)})

    # Pass: 静态上下文字段修正
    for m in _re.finditer(r'([^\s:]+\.java):\d+: (?:错误|error): 无法从静态上下文中引用非静态 变量 (\w+)\n\s*位置: +类 ([\w.]+)', text):
        fname, tgt = m.group(2), m.group(3)
        fqn = stub_names.get(tgt)
        if fqn and fqn in state:
            info = state[fqn]
            if fname in info.get('fields', []):
                info['fields'] = [f for f in info['fields'] if f != fname] + [fname]
                info['staticfields'] = info.get('staticfields', []) + [fname]

    for line in open(os.path.join(ROOT, 'build/harvest_imports.txt')):
        line = line.strip()
        m = re.match(r'(androidx\.media3[\w.]*)\.(\w+)$', line)
        if not m: continue
        pkg, name = m.group(1), m.group(2)
        if pkg not in pkgs: continue
        parts = pkg.rsplit('.', 1)
        if len(parts) == 2 and parts[1][:1].isupper():
            pkey = parts[0] + '.' + parts[1]
            if pkey in state:
                if name not in state[pkey].setdefault('nested', []): state[pkey]['nested'].append(name)
                continue
        state.setdefault(pkg + '.' + name, {'pkg': pkg, 'name': name, 'methods': [], 'fields': [], 'ctors': [], 'nested': []})

    save_state(state)
    for key, info in state.items(): gen_file(info['pkg'], info['name'], info)
    json.dump({k: sorted(v) for k, v in unresolved.items()}, open(os.path.join(ROOT, 'build/unresolved.json'), 'w'), ensure_ascii=False, indent=1)
    print(f'stubs total={len(state)} unresolved={ {k: len(v) for k, v in unresolved.items()} }')

if __name__ == '__main__':
    main()
