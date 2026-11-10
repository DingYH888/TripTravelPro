export function resolveImage(image?: string): string {
  if (!image) {
    return '/frontdesk/images/theme1.jpg'
  }
  if (image.startsWith('http://') || image.startsWith('https://') || image.startsWith('/')) {
    return image
  }
  return `/${image}`
}

export function formatPrice(price?: number): string {
  return price == null ? '-' : String(price)
}
