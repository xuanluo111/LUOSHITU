#!/usr/bin/env python3
# 解析 gradle javac 输出，自动生成/修正 androidx.media3 补齐桩（O2 决策，见 docs/S0-立项报告.md §9）
import re, sys, os, json

LOG = sys.argv[1]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STUB = os.path.join(ROOT, 'app/src/main/java')
STATE = os.path.join(ROOT, 'build/stubstate.json')

def load_state():
    raw = json.load(open(STATE)) if os.path.exists(STATE) else {}
    out = {}
    for k, v in raw.items():
        if not k.startswith('androidx.media3.'): continue
        f = os.path.join(STUB, *k.split('.')) + '.java'
        if os.path.exists(f) and '@hand' in open(f, encoding='utf-8', errors='replace').read(2048): continue
        out[k] = v
    return out

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
        if not a or '[' in a or '<' in a or '(' in a or '?' in a or '::' in a: a = 'Object'
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
    try:
        seeds = json.load(open(os.path.join(ROOT, 'build/classmap.json')))
        for k2, v2 in seeds.items(): imp_map.setdefault(k2, v2)
    except Exception: pass
    fixed = 0
    for m in re.finditer(r'([^\s:]+\.java):\d+: (?:错误|error): 找不到符号\n(?:.*\n)*?\s*符号: +类 ([\w.]+)\n\s*位置: +类 ([\w.]+)', text):
        fpath, cls_name = m.group(1), m.group(2)
        simple = cls_name.rsplit('.', 1)[-1]
        fqn = imp_map.get(simple)
        if fqn and '.' in fqn and ('/androidx/media3/' in fpath or '/luoshitu/' in fpath):
            try:
                content = open(fpath.split(':')[0], encoding='utf-8').read()
                if 'import ' + fqn + ';' not in content:
                    content = re.sub(r'(package [\w.]+;)', lambda mm: mm.group(1) + '\n\nimport ' + fqn + ';', content, count=1)
                    open(fpath.split(':')[0], 'w', encoding='utf-8').write(content)
                    fixed += 1
            except Exception: pass
    print('imports fixed:', fixed)

if __name__ == '__main__':
    main()
