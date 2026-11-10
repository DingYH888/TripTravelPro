const ALLOWED_TAGS = new Set([
  'P', 'BR', 'STRONG', 'B', 'EM', 'I', 'U', 'S', 'H1', 'H2', 'H3', 'H4',
  'UL', 'OL', 'LI', 'A', 'IMG', 'SPAN', 'DIV', 'BLOCKQUOTE', 'PRE', 'CODE',
  'TABLE', 'THEAD', 'TBODY', 'TR', 'TD', 'TH', 'HR'
])

const GLOBAL_ATTRS = new Set(['style', 'class', 'title'])
const TAG_ATTRS: Record<string, Set<string>> = {
  A: new Set(['href', 'target', 'rel']),
  IMG: new Set(['src', 'alt', 'width', 'height']),
  TD: new Set(['colspan', 'rowspan']),
  TH: new Set(['colspan', 'rowspan'])
}

function isSafeUrl(value: string) {
  const url = value.trim().toLowerCase()
  return url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/')
    || url.startsWith('#') || url.startsWith('mailto:')
}

/**
 * 富文本展示前的白名单清洗，移除脚本、事件属性与危险协议。
 */
export function sanitizeHtml(input: string): string {
  if (!input) {
    return ''
  }
  const doc = new DOMParser().parseFromString(input, 'text/html')
  for (const element of Array.from(doc.body.querySelectorAll('*'))) {
    if (!ALLOWED_TAGS.has(element.tagName)) {
      element.remove()
      continue
    }
    for (const attribute of Array.from(element.attributes)) {
      const name = attribute.name.toLowerCase()
      const allowed = GLOBAL_ATTRS.has(name) || TAG_ATTRS[element.tagName]?.has(name)
      if (!allowed || name.startsWith('on')) {
        element.removeAttribute(attribute.name)
        continue
      }
      if ((name === 'href' || name === 'src') && !isSafeUrl(attribute.value)) {
        element.removeAttribute(attribute.name)
      }
      if (name === 'style' && /expression|url\s*\(|javascript:/i.test(attribute.value)) {
        element.removeAttribute('style')
      }
    }
    if (element.tagName === 'A') {
      element.setAttribute('rel', 'noopener noreferrer')
    }
  }
  return doc.body.innerHTML
}
