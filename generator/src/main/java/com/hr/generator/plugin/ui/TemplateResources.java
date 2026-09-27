package com.hr.generator.plugin.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 把打包进插件的模板（template/、vm/、scaffold/）解压到临时目录，
 * 供 {@link com.hr.generator.TemplateEngine} 按文件系统路径加载。
 * 开发态资源本身就在磁盘上时直接返回原路径，免去复制。
 */
final class TemplateResources {

    private TemplateResources() {}

    private static final String[] ROOTS = {"template/", "vm/", "scaffold/"};

    private static volatile Path cached;

    static Path extract() throws IOException {
        Path ready = cached;
        if (ready != null && Files.isDirectory(ready.resolve("template"))) {
            return ready;
        }
        synchronized (TemplateResources.class) {
            if (cached != null && Files.isDirectory(cached.resolve("template"))) {
                return cached;
            }
            URL marker = TemplateResources.class.getResource("/template/entity.java.ftl");
            if (marker == null) {
                throw new IOException("插件内找不到模板资源 /template/entity.java.ftl");
            }
            if ("file".equalsIgnoreCase(marker.getProtocol())) {
                cached = fileRoot(marker);
                return cached;
            }
            cached = extractJarUrl(marker);
            return cached;
        }
    }

    /** file URL 形如 .../resources/template/entity.java.ftl，向上两级即资源根。 */
    private static Path fileRoot(URL marker) throws IOException {
        String spec = marker.toString();
        if (spec.contains("!/")) {
            return extractJarUrl(marker);
        }
        try {
            Path file = Path.of(marker.toURI());
            return file.getParent().getParent();
        } catch (java.net.URISyntaxException e) {
            throw new IOException("无法解析模板路径: " + marker, e);
        }
    }

    private static Path extractJarUrl(URL marker) throws IOException {
        Path root = Files.createTempDirectory("mybatis-terminator-templates");
        root.toFile().deleteOnExit();
        extractFromJar(marker, root);
        return root;
    }

    /**
     * 按 jar 条目逐个复制。不能用目录 URI 打开文件系统：IDEA 的插件类加载器给出的
     * jar URL 不带 jar: 前缀，按目录截取会定位错资源，点号开头的文件也会丢。
     */
    private static void extractFromJar(URL marker, Path root) throws IOException {
        JarFile jar = ((JarURLConnection) new URL("jar:" + jarBase(marker) + "!/").openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!underTemplateRoot(name) || name.contains("node_modules/")) {
                continue;
            }
            Path dest = root.resolve(name);
            if (entry.isDirectory()) {
                Files.createDirectories(dest);
                continue;
            }
            Files.createDirectories(dest.getParent());
            try (InputStream in = jar.getInputStream(entry)) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            dest.toFile().deleteOnExit();
        }
    }

    /** 资源 URL 形如 jar:file:/x.jar!/template/a.ftl 或 file:/x.jar!/template/a.ftl，取 jar 文件部分。 */
    private static String jarBase(URL marker) {
        String spec = marker.toString();
        int bang = spec.indexOf("!/");
        if (bang < 0) {
            throw new IllegalStateException("不是 jar 内资源: " + spec);
        }
        String base = spec.substring(0, bang);
        return base.startsWith("jar:") ? base.substring("jar:".length()) : base;
    }

    private static boolean underTemplateRoot(String name) {
        for (String root : ROOTS) {
            if (name.startsWith(root)) {
                return true;
            }
        }
        return false;
    }

    /** 供测试读取解压结果。 */
    static String read(Path root, String relative) throws IOException {
        return Files.readString(root.resolve(relative), StandardCharsets.UTF_8);
    }
}
