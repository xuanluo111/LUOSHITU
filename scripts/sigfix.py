#!/usr/bin/env python3
# 解析 javac 全量日志，把 @hand 桩里的方法签名/返回类型/静态性修正为调用点真实形态。
# 仅写 app/src/main/java/androidx 下的文件。
import re, sys, os

LOG = sys.argv[1]
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STUB = os.path.join(ROOT, 'app/src/main/java/androidx')

def stub_files():
    out = {}
    for dirpath, _, files in os.walk(STUB):
        for fn in files:
            if fn.endswith('.java'):
                p = os.path.join(dirpath, fn)
                out[fn[:-5]] = p
    return out

def read(p):
    return open(p, encoding='utf-8').read()

def write(p, s):
    open(p, 'w', encoding='utf-8').write(s)

def replace_method(files, cls, name, new_params=None, new_ret=None, static=None, occurrence='first'):
    """在类名为 cls 的桩文件里改方法签名；cls 为短名。"""
    p = files.get(cls)
    if not p: return False
    s = read(p)
    pat = re.compile(r'(public (?:static )?)((?:[\w.<>\[\], ]+?) )?' + re.escape(name) + r'\(([^)]*)\)')
    ms = list(pat.finditer(s))
    if not ms: return False
    m = ms[0] if occurrence == 'first' else ms[-1]
    old = m.group(0)
    prefix = m.group(1) or 'public '
    ret = m.group(2) or ''
    if new_ret is not None: ret = new_ret + ' ' if new_ret else ''
    if static is True and 'static' not in prefix: prefix = prefix.replace('public ', 'public static ', 1)
    if static is False: prefix = prefix.replace('static ', '', 1)
    params = m.group(3)
    if new_params is not None: params = new_params
    new = prefix + ret + name + '(' + params + ')'
    s = s[:m.start()] + new + s[m.end():]
    write(p, s)
    return True

def add_method(files, cls, sig):
    p = files.get(cls)
    if not p: return False
    s = read(p)
    if sig in s: return True
    s = s.replace('\n}\n', '\n\t' + sig + ' { }\n}\n')
    write(p, s)
    return True

def src_line(fpath, lineno):
    try: return open(fpath.split(':')[0], encoding='utf-8', errors='replace').read().split('\n')[int(lineno) - 1]
    except Exception: return ''

def main():
    text = open(LOG, encoding='utf-8', errors='replace').read()
    files = stub_files()
    loglines = text.split('\n')
    fixed = 0

    # 1) 方法应用失败：需要/发现 → 以"发现"为准确参
    for m in re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 无法将类 ([\w.]+)中的方法 (\w+)应用到给定类型;\n((?:[^\n]*\n)*?)\s*原因:', text):
        fpath, lineno, cls, name, body = m.group(1), m.group(2), m.group(3), m.group(4), m.group(5)
        fm = re.search(r'(?:发现|找到): +([^\n]+)', body)
        if not fm: continue
        found = fm.group(1).strip()
        if '没有参数' in found: params = ''
        else:
            params = found
            # 清洗：非类型 token → Object
            parts = []
            depth = 0; cur = ''
            for ch in found + ',':
                if ch == '<': depth += 1
                if ch == '>': depth -= 1
                if ch == ',' and depth == 0:
                    parts.append(cur.strip()); cur = ''
                else: cur += ch
            fixed_parts = []
            for a in parts:
                a = a.strip()
                if not a or '?' in a or '::' in a or '(' in a or a.startswith('Convert'): a = 'Object'
                fixed_parts.append(a)
            params = ', '.join(fixed_parts)
        # 参数名化
        if params:
            named = ', '.join(f'{a.strip()} p{i}' for i, a in enumerate([x.strip() for x in params.split(',') if x.strip()]))
        else: named = ''
        if replace_method(files, cls.rsplit('.', 1)[-1], name, new_params=named): fixed += 1

    # 2) 返回类型：Object无法转换为 X
    for m in re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 不兼容的类型: ?Object无法转换为 ?([\w.<>\[\], ]+)', text):
        fpath, lineno, want = m.group(1), m.group(2), m.group(3).strip()
        line = src_line(fpath, lineno)
        nm = re.search(r'\.(\w+)\(', line)
        if not nm: continue
        name = nm.group(1)
        for cls in set(re.findall(r'[\w.]*\.(\w+)', line)):
            pass
        # 尝试行上出现的所有可能是桩类的名字
        for tok in re.findall(r'(\w+)\.', line):
            if tok in files and replace_method(files, tok, name, new_ret=want): fixed += 1; break

    # 3) '==' 操作数：第二个类型即期望
    for m in re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 二元运算符 [^\n]*?第二个类型: ?([\w.<>\[\], ]+)', text):
        fpath, lineno, want = m.group(1), m.group(2), m.group(3).strip()
        line = src_line(fpath, lineno)
        nm = re.search(r'\.(\w+)\(', line)
        if not nm: continue
        name = nm.group(1)
        for tok in re.findall(r'(\w+)\.', line):
            if tok in files and replace_method(files, tok, name, new_ret=want): fixed += 1; break

    # 4) 静态上下文：变量/方法 → 加 static
    for m in re.finditer(r'([^\s:]+\.java):\d+: (?:错误|error): 无法从静态上下文中引用非静态 (变量|方法) (\w+)(?:\(([^)]*)\))?\n\s*位置: +类 ([\w.]+)', text):
        kind, name, params, loc = m.group(2), m.group(3), m.group(4), m.group(5)
        cls = loc.rsplit('.', 1)[-1]
        if replace_method(files, cls, name, static=True, occurrence='last'): fixed += 1
        else:
            p = files.get(cls)
            if p:
                s = read(p)
                mm = re.search(r'((?:public|\tpublic)[^\n]*\b' + re.escape(name) + r'\b[^\n]*;)', s)
                if mm and 'static' not in mm.group(1):
                    s = s.replace(mm.group(1), mm.group(1).replace('public ', 'public static ', 1), 1)
                    write(p, s); fixed += 1

    # 5) 方法不会覆盖：以 App 侧签名为准改桩
    for m in re.finditer(r'([^\s:]+\.java):(\d+): (?:错误|error): 方法不会覆盖或实现超类型的方法\n((?:[^\n]*\n)*?)\n', text):
        fpath, lineno, body = m.group(1), m.group(2), m.group(3)
        dm = re.search(r'public\s+(?:static\s+)?([\w.<>\[\], ]+?)\s+(\w+)\(([^)]*)\)', body)
        if not dm: continue
        ret, name, params = dm.group(1).strip(), dm.group(2), dm.group(3).strip()
        # 找声明该方法的桩：参数类型里出现的类型名
        cand = None
        for tok in re.findall(r'([\w.]+)', params) + [ret]:
            base = tok.split('.')[-1].split('<')[0]
            if base in files:
                s = read(files[base])
                if re.search(r'\b' + name + r'\(', s): cand = base; break
        if cand and replace_method(files, cand, name, new_params=params, new_ret=ret): fixed += 1

    print(f'sigfix applied: {fixed}')

if __name__ == '__main__':
    main()
