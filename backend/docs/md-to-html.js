#!/usr/bin/env node

/**
 * Markdown 转 HTML 生成器
 * 用于将设计文档转换为 HTML 供文档中心访问
 */

import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));

// 简单的 Markdown 转 HTML 函数
function markdownToHtml(md) {
  let html = md;
  
  // 转义 HTML 特殊字符
  html = html.replace(/&/g, '&amp;')
             .replace(/</g, '&lt;')
             .replace(/>/g, '&gt;');
  
  // 标题
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
  html = html.replace(/^#### (.+)$/gm, '<h4>$1</h4>');
  html = html.replace(/^##### (.+)$/gm, '<h5>$1</h5>');
  html = html.replace(/^###### (.+)$/gm, '<h6>$1</h6>');
  
  // 粗体和斜体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
  html = html.replace(/~~(.+?)~~/g, '<del>$1</del>');
  
  // 代码块
  html = html.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang || 'plaintext'}">${code.trim()}</code></pre>`;
  });
  
  // 行内代码
  html = html.replace(/`(.+?)`/g, '<code class="inline-code">$1</code>');
  
  // 引用
  html = html.replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>');
  
  // 链接
  html = html.replace(/\[(.+?)\]\((.+?)\)/g, '<a href="$2" target="_blank">$1</a>');
  
  // 图片
  html = html.replace(/!\[(.+?)\]\((.+?)\)/g, '<img src="$2" alt="$1" />');
  
  // 列表
  html = html.replace(/^\s*[-*+]\s+(.+)$/gm, '<li>$1</li>');
  html = html.replace(/^\s*\d+\.\s+(.+)$/gm, '<li>$1</li>');
  
  // 表格 (简化处理)
  html = html.replace(/^\|(.+)\|$/gm, (match, content) => {
    const cells = content.split('|').map(cell => cell.trim());
    return `<tr>${cells.map(cell => `<td>${cell}</td>`).join('')}</tr>`;
  });
  
  // 水平线
  html = html.replace(/^---+$/gm, '<hr />');
  
  // 段落
  html = html.replace(/\n\n/g, '</p><p>');
  html = '<p>' + html + '</p>';
  
  // 清理空段落
  html = html.replace(/<p>\s*<\/p>/g, '');
  html = html.replace(/<p>(<h[1-6]>)/g, '$1');
  html = html.replace(/(<\/h[1-6]>)<\/p>/g, '$1');
  html = html.replace(/<p>(<pre>)/g, '$1');
  html = html.replace(/(<\/pre>)<\/p>/g, '$1');
  html = html.replace(/<p>(<blockquote>)/g, '$1');
  html = html.replace(/(<\/blockquote>)<\/p>/g, '$1');
  html = html.replace(/<p>(<ul>)/g, '$1');
  html = html.replace(/(<\/ul>)<\/p>/g, '$1');
  html = html.replace(/<p>(<ol>)/g, '$1');
  html = html.replace(/(<\/ol>)<\/p>/g, '$1');
  html = html.replace(/<p>(<table>)/g, '$1');
  html = html.replace(/(<\/table>)<\/p>/g, '$1');
  html = html.replace(/<p>(<tr>)/g, '$1');
  html = html.replace(/(<\/tr>)<\/p>/g, '$1');
  
  return html;
}

// 生成完整的 HTML 文档
function generateHtmlDocument(title, content, outputPath) {
  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            line-height: 1.8;
            color: #333;
            background: #f5f7fa;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: #fff;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
        }
        
        h1 {
            font-size: 28px;
            color: #303133;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid #409eff;
        }
        
        h2 {
            font-size: 22px;
            color: #303133;
            margin: 30px 0 15px;
            padding-left: 10px;
            border-left: 4px solid #409eff;
        }
        
        h3 {
            font-size: 18px;
            color: #303133;
            margin: 20px 0 10px;
        }
        
        h4 {
            font-size: 16px;
            color: #606266;
            margin: 15px 0 8px;
        }
        
        p {
            margin: 10px 0;
            text-align: justify;
        }
        
        ul, ol {
            margin: 10px 0 10px 25px;
        }
        
        li {
            margin: 5px 0;
        }
        
        blockquote {
            margin: 15px 0;
            padding: 10px 20px;
            background: #f0f9eb;
            border-left: 4px solid #67c23a;
            border-radius: 4px;
        }
        
        .code-block {
            margin: 15px 0;
            padding: 15px;
            background: #282c34;
            color: #abb2bf;
            border-radius: 4px;
            overflow-x: auto;
            font-family: "Fira Code", "Consolas", monospace;
            font-size: 13px;
            line-height: 1.5;
        }
        
        .inline-code {
            padding: 2px 6px;
            background: #f5f7fa;
            border: 1px solid #e4e7ed;
            border-radius: 3px;
            font-family: "Fira Code", "Consolas", monospace;
            font-size: 13px;
            color: #e83e8c;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        
        th, td {
            border: 1px solid #dcdfe6;
            padding: 12px;
            text-align: left;
        }
        
        th {
            background: #f5f7fa;
            font-weight: 600;
            color: #606266;
        }
        
        tr:nth-child(even) {
            background: #fafafa;
        }
        
        tr:hover {
            background: #f5f7fa;
        }
        
        hr {
            margin: 30px 0;
            border: none;
            border-top: 1px solid #e4e7ed;
        }
        
        a {
            color: #409eff;
            text-decoration: none;
        }
        
        a:hover {
            text-decoration: underline;
        }
        
        .header-meta {
            background: #ecf5ff;
            padding: 15px 20px;
            border-radius: 4px;
            margin-bottom: 30px;
            border-left: 4px solid #409eff;
        }
        
        .header-meta p {
            margin: 5px 0;
            font-size: 14px;
            color: #606266;
        }
        
        .toc {
            background: #fafafa;
            padding: 20px;
            border-radius: 4px;
            margin: 20px 0;
        }
        
        .toc h3 {
            margin-top: 0;
        }
        
        .toc ul {
            list-style: none;
        }
        
        .toc li {
            margin: 8px 0;
        }
        
        .toc a {
            color: #606266;
        }
        
        .toc a:hover {
            color: #409eff;
        }
        
        .warning {
            background: #fdf6ec;
            border-left: 4px solid #e6a23c;
            padding: 15px 20px;
            margin: 20px 0;
            border-radius: 4px;
        }
        
        .info {
            background: #ecf5ff;
            border-left: 4px solid #409eff;
            padding: 15px 20px;
            margin: 20px 0;
            border-radius: 4px;
        }
        
        .success {
            background: #f0f9eb;
            border-left: 4px solid #67c23a;
            padding: 15px 20px;
            margin: 20px 0;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <div class="container">
        ${content}
    </div>
    
    <script>
        // 自动生成目录
        document.addEventListener('DOMContentLoaded', function() {
            const headings = document.querySelectorAll('h2, h3');
            if (headings.length === 0) return;
            
            const toc = document.createElement('div');
            toc.className = 'toc';
            toc.innerHTML = '<h3>📑 目录</h3><ul></ul>';
            
            const ul = toc.querySelector('ul');
            headings.forEach((heading, index) => {
                const id = 'heading-' + index;
                heading.id = id;
                
                const li = document.createElement('li');
                li.style.marginLeft = heading.tagName === 'H3' ? '20px' : '0';
                li.innerHTML = '<a href="#' + id + '">' + heading.textContent + '</a>';
                ul.appendChild(li);
            });
            
            document.querySelector('h1').insertAdjacentElement('afterend', toc);
        });
    </script>
</body>
</html>`;

  // 确保输出目录存在
  const outputDir = dirname(outputPath);
  if (!existsSync(outputDir)) {
    mkdirSync(outputDir, { recursive: true });
  }
  
  writeFileSync(outputPath, html, 'utf-8');
  console.log(`✓ HTML 文档已生成：${outputPath}`);
}

// 主函数
function main() {
  const args = process.argv.slice(2);
  
  if (args.length < 2) {
    console.log('用法：node md-to-html.js <input.md> <output.html>');
    process.exit(1);
  }
  
  const inputPath = args[0];
  const outputPath = args[1];
  
  console.log(`正在转换：${inputPath}`);
  
  try {
    const mdContent = readFileSync(inputPath, 'utf-8');
    const title = mdContent.match(/^#\s+(.+)/)?.[1] || '文档';
    const htmlContent = markdownToHtml(mdContent);
    
    // 添加文档元信息
    const metaHtml = `
        <div class="header-meta">
            <p><strong>📄 文档</strong>：${title}</p>
            <p><strong>📅 生成时间</strong>：${new Date().toLocaleString('zh-CN')}</p>
        </div>
    ` + htmlContent;
    
    generateHtmlDocument(title, metaHtml, outputPath);
  } catch (error) {
    console.error('错误:', error.message);
    process.exit(1);
  }
}

main();
