package com.hr.generator.plugin.ui;

import java.nio.file.Files;
import java.nio.file.Path;

/** 用打好的插件 jar 验证模板解压，模拟 IDEA 安装后的资源布局。 */
public final class TemplateExtractCheck {
    private TemplateExtractCheck() {}

    public static void main(String[] args) throws Exception {
        Path jar = Path.of(args[0]);
        if (!Files.isRegularFile(jar)) {
            throw new IllegalStateException("插件 jar 不存在: " + jar);
        }
        Thread.currentThread().setContextClassLoader(new java.net.URLClassLoader(new java.net.URL[]{jar.toUri().toURL()}) {
            @Override
            public java.net.URL getResource(String name) {
                return findResource(name);
            }
        });
        Path root = TemplateResources.extract();
        String[] required = {
            "template/entity.java.ftl",
            "vm/api.ts.vm",
            "vm/index.join.js.vm",
            "scaffold/backend/pom.xml",
            "scaffold/frontend/gitignore.txt",
            "scaffold/frontend/package.json"
        };
        for (String name : required) {
            if (!Files.isRegularFile(root.resolve(name))) {
                throw new IllegalStateException("解压后缺少: " + name);
            }
        }
        long copied = Files.walk(root).filter(Files::isRegularFile).count();
        System.out.println("Template extract check passed, files=" + copied);
    }
}
