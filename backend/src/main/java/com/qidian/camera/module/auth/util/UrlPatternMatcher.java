package com.qidian.camera.module.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * URL 模式匹配工具类
 * 支持通配符和路径变量匹配
 * 
 * @author qidian
 * @since 1.0.0
 */
@Slf4j
@Component
public class UrlPatternMatcher {
    
    /**
     * 匹配请求 URI 是否符合给定的 URI 模式
     * 
     * @param pattern URI 模式，支持通配符 ** 和路径变量 {id}
     * @param uri 实际请求的 URI
     * @return 是否匹配
     */
    public boolean matches(String pattern, String uri) {
        if (pattern == null || uri == null) {
            return false;
        }
        
        // 如果完全相等，直接返回 true
        if (pattern.equals(uri)) {
            return true;
        }
        
        // 将 URI 模式转换为正则表达式
        String regex = convertToRegex(pattern);
        
        try {
            return Pattern.matches(regex, uri);
        } catch (Exception e) {
            log.warn("URL 模式匹配失败：pattern={}, uri={}", pattern, uri, e);
            return false;
        }
    }
    
    /**
     * 将 URI 模式转换为正则表达式
     * 
     * 转换规则：
     * - ** 匹配任意路径（包括多级）
     * - * 匹配单级路径
     * - {xxx} 匹配路径变量
     * 
     * @param pattern URI 模式
     * @return 正则表达式
     */
    private String convertToRegex(String pattern) {
        // 转义正则特殊字符（除了 * 和 {}）
        StringBuilder regex = new StringBuilder();
        regex.append("^");
        
        int i = 0;
        while (i < pattern.length()) {
            char c = pattern.charAt(i);
            
            // 处理路径变量 {xxx}
            if (c == '{') {
                int endIndex = pattern.indexOf('}', i);
                if (endIndex != -1) {
                    // 匹配任意非 / 字符
                    regex.append("[^/]+");
                    i = endIndex + 1;
                    continue;
                }
            }
            
            // 处理双星号 **
            if (c == '*' && i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                // ** 匹配任意路径（包括 /）
                regex.append(".*");
                i += 2;
                continue;
            }
            
            // 处理单星号 *
            if (c == '*') {
                // * 匹配单级路径（不包括 /）
                regex.append("[^/]*");
                i++;
                continue;
            }
            
            // 转义其他特殊字符
            if ("\\.[]()^$+?|".indexOf(c) != -1) {
                regex.append('\\');
            }
            regex.append(c);
            i++;
        }
        
        regex.append("$");
        return regex.toString();
    }
}
