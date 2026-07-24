import { readdir, readFile, stat } from 'node:fs/promises'
import path from 'node:path'

const dist = path.resolve('frontend/dist')
await stat(path.join(dist, 'index.html'))
const files = await readdir(path.join(dist, 'assets'))
const contents = await Promise.all(files.map(file => readFile(path.join(dist, 'assets', file), 'utf8')))
const index = await readFile(path.join(dist, 'index.html'), 'utf8')
const javascript = contents.filter((_, index) => files[index].endsWith('.js')).join('\n')
const lazyChunk = files.find(file => file.startsWith('DemoModeStatus-') && file.endsWith('.js'))
if (!index.includes('/School-Project/assets/') || !javascript.includes('/School-Project/') || !lazyChunk || !javascript.includes(`./${lazyChunk}`) || contents.some(content => /https?:\/\/(?:localhost|127\.0\.1)/i.test(content))) {
  throw new Error('Pages artifact does not use /School-Project/ or contains a localhost API reference')
}
console.log(`Verified ${path.relative(process.cwd(), dist)} for GitHub Pages`)