package com.hr.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 渲染 vm/ 下的前端模板。
 *
 * 不用 Velocity：IDEA 平台自带一份 Velocity，插件再打包一份时两个类加载器
 * 加载出同名类，初始化即抛 ClassCastException。这里只实现模板实际用到的语法：
 * 变量替换、#foreach 循环、#if/#else/#end 分支。
 */
final class VmRenderer {
  private VmRenderer() {}

  static String render(Path template, Map<String, Object> model) throws IOException {
    String source = Files.readString(template, StandardCharsets.UTF_8);
    return renderBlock(source, model);
  }

  private static String renderBlock(String source, Map<String, Object> model) {
    StringBuilder out = new StringBuilder();
    int i = 0;
    while (i < source.length()) {
      int dir = source.indexOf('#', i);
      if (dir < 0) {
        out.append(substitute(source.substring(i), model));
        break;
      }
      out.append(substitute(source.substring(i, dir), model));
      if (source.startsWith("#foreach", dir)) {
        int headerEnd = source.indexOf('\n', dir);
        String header = source.substring(dir, headerEnd);
        int bodyEnd = matchingEnd(source, headerEnd + 1);
        Matcher m = Pattern.compile("#foreach\\(\\s*\\$(\\w+)\\s+in\\s+\\$(\\w+)\\s*\\)").matcher(header);
        if (!m.find()) {
          throw new IllegalStateException("无法解析模板指令: " + header.trim());
        }
        Object items = model.get(m.group(2));
        if (!(items instanceof List<?> list)) {
          throw new IllegalStateException("循环变量不是列表: $" + m.group(2));
        }
        String body = source.substring(headerEnd + 1, bodyEnd);
        for (Object item : list) {
          Map<String, Object> scope = new java.util.HashMap<>(model);
          scope.put(m.group(1), item);
          out.append(renderBlock(body, scope));
        }
        i = bodyEnd + "#end".length();
      } else if (source.startsWith("#if", dir)) {
        int headerEnd = source.indexOf('\n', dir);
        String header = source.substring(dir, headerEnd);
        int bodyEnd = matchingEnd(source, headerEnd + 1);
        String body = source.substring(headerEnd + 1, bodyEnd);
        int elseAt = topLevelElse(body);
        String whenTrue = elseAt < 0 ? body : body.substring(0, elseAt);
        String whenFalse = elseAt < 0 ? "" : body.substring(elseAt + "#else".length());
        out.append(renderBlock(condition(header, model) ? whenTrue : whenFalse, model));
        i = bodyEnd + "#end".length();
      } else {
        out.append('#');
        i = dir + 1;
      }
    }
    return out.toString();
  }

  /** 找到与当前位置配对的 #end（跳过嵌套的 #foreach/#if）。 */
  private static int matchingEnd(String source, int from) {
    int depth = 1;
    int i = from;
    while (i < source.length()) {
      int mark = source.indexOf('#', i);
      if (mark < 0) break;
      if (source.startsWith("#foreach", mark) || source.startsWith("#if", mark)) {
        depth++;
      } else if (source.startsWith("#end", mark)) {
        depth--;
        if (depth == 0) return mark;
      }
      i = mark + 1;
    }
    throw new IllegalStateException("模板缺少配对的 #end");
  }

  /** body 内与本层 #if 配对的 #else 位置，没有返回 -1。 */
  private static int topLevelElse(String body) {
    int depth = 0;
    int i = 0;
    while (i < body.length()) {
      int mark = body.indexOf('#', i);
      if (mark < 0) break;
      if (body.startsWith("#foreach", mark) || body.startsWith("#if", mark)) {
        depth++;
      } else if (body.startsWith("#end", mark)) {
        depth--;
      } else if (body.startsWith("#else", mark) && depth == 0) {
        return mark;
      }
      i = mark + 1;
    }
    return -1;
  }

  private static boolean condition(String header, Map<String, Object> model) {
    Matcher m = Pattern.compile("#if\\(\\$([\\w.]+)\\.size\\(\\)\\s*==\\s*(\\d+)\\)").matcher(header);
    if (!m.find()) {
      throw new IllegalStateException("不支持的条件指令: " + header.trim());
    }
    Object value = resolve(m.group(1), model);
    if (!(value instanceof List<?> list)) {
      throw new IllegalStateException("size() 只能用于列表: $" + m.group(1));
    }
    return list.size() == Integer.parseInt(m.group(2));
  }

  /** 替换 $var、${var}、${obj.prop}、${list.get(n).prop}。 */
  private static String substitute(String text, Map<String, Object> model) {
    Matcher m = Pattern.compile("\\$\\{([^}]+)}|\\$([A-Za-z_][\\w.()]*)").matcher(text);
    StringBuilder out = new StringBuilder();
    while (m.find()) {
      String expr = m.group(1) != null ? m.group(1) : m.group(2);
      Object value = resolve(expr, model);
      m.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
    }
    m.appendTail(out);
    return out.toString();
  }

  private static Object resolve(String expr, Map<String, Object> model) {
    List<String> tokens = new ArrayList<>();
    Matcher m = Pattern.compile("[A-Za-z_]\\w*|\\d+").matcher(expr);
    while (m.find()) tokens.add(m.group());
    if (tokens.isEmpty()) return null;
    Object current = model.get(tokens.get(0));
    for (int i = 1; i < tokens.size() && current != null; i++) {
      String token = tokens.get(i);
      if ("get".equals(token) && current instanceof List<?>) {
        continue;
      } else if (current instanceof List<?> list) {
        current = list.get(Integer.parseInt(token));
      } else if (current instanceof Map<?, ?> map) {
        current = map.get(token);
      } else {
        current = readProperty(current, token);
      }
    }
    return current;
  }

  private static Object readProperty(Object target, String name) {
    String method = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    try {
      return target.getClass().getMethod(method).invoke(target);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("模板变量无法解析: " + target.getClass().getSimpleName() + "." + name, e);
    }
  }
}
