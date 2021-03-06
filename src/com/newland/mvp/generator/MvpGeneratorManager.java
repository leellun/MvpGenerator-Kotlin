package com.newland.mvp.generator;

import com.intellij.openapi.module.Module;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MvpGeneratorManager {
    private static volatile MvpGeneratorManager instance;

    public static MvpGeneratorManager getInstance() {
        if (instance == null) {
            synchronized (MvpGeneratorManager.class) {
                if (instance == null) {
                    instance = new MvpGeneratorManager();
                }
            }
        }
        return instance;
    }

    private MvpGeneratorManager() {
    }

    public boolean isExistsProperties(Module module) {
        String modelBasePath = module.getModuleFile().getParent().getPath();
        File file = new File(modelBasePath + File.separator + "mvpgenerator.properties");
        if (!file.exists()) {
            file = new File(module.getProject().getBasePath() + File.separator + "mvpgenerator.properties");
        }
        return file.exists();
    }

    public synchronized GeneratorProperties getProperties(Module module) {
        String modelBasePath = module.getModuleFile().getParent().getPath();
        GeneratorProperties properties = new GeneratorProperties();
        try {
            File file = new File(modelBasePath + File.separator + "mvpgenerator.properties");
            if (!file.exists()) {
                file = new File(module.getProject().getBasePath() + File.separator + "mvpgenerator.properties");
            }
            properties.load(new FileInputStream(file));
            properties.put("PROJECT_PATH", module.getProject().getBasePath());
            properties.put("MODULE_PATH", modelBasePath);
        } catch (IOException e) {
//            throw new RuntimeException("Can not find mvpgenerator.properties in project or current module", e);
        }
        return properties;
    }

    public synchronized void writeMvpProperties(Module module, Map<String, String> map) {
        File file = new File(module.getProject().getBasePath() + File.separator + "mvpgenerator.properties");
        // ???????????????
        try {
            Properties properties;
            if (file.exists()) {
                properties = getProperties(module);
                properties.remove("MODULE_PATH");
                properties.remove("PROJECT_PATH");
            } else {
                properties = new Properties();
            }
            FileOutputStream fos = new FileOutputStream(file);
            for (String key : map.keySet()) {
                properties.setProperty(key, map.get(key));
            }
            properties.store(fos, "Copyright (c) Android Studio");
            fos.close();// ?????????
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static class GeneratorProperties extends Properties {
        private static final String MVP_HELPER_PACKAGE = "mvp.helper.package";
        private static final String MVP_ACTIVITY_PACKAGE = "mvp.activity.package";
        private static final String MVP_FRAGMENT_PACKAGE = "mvp.fragment.package";
        public static final String MVP_ACTIVITY_NAME = "mvp.activity.name";
        public static final String MVP_FRAGMENT_NAME = "mvp.fragment.name";
        private static final String ACTIVITY_INJECTOR_FACTORY_FILE = "activity.injector.factory.file";
        private static final String FRAGMENT_INJECTOR_FACTORY_FILE = "fragment.injector.factory.file";
        private static final String MANIFEST_ACTIVITY_TAG_ATTRS = "manifest.activity.tag.attrs";
        private static final String LAYOUT_FOLDERS = "layout.folders";

        public String getCommonPackage() {
            return getProperty(MVP_HELPER_PACKAGE);
        }

        public String getMvpActivityPackage() {
            return getProperty(MVP_ACTIVITY_PACKAGE);
        }

        public String getMvpFragmentPackage() {
            return getProperty(MVP_FRAGMENT_PACKAGE);
        }

        public Map<String, String> getManifestActivityTagAttrs() {
            Map<String, String> map = new HashMap<>();
            if (containsKey(MANIFEST_ACTIVITY_TAG_ATTRS)) {
                String attrString = getProperty(MANIFEST_ACTIVITY_TAG_ATTRS);
                attrString = attrString.replaceAll("\\s", "");
                attrString = attrString.replaceAll("(\\[)([^\\]]+)(\\])", "$2");
                String[] attrs = attrString.split(",");
                for (String attr : attrs) {
                    String[] kv = attr.split("=");
                    map.put(kv[0], kv[1]);
                }
            }
            return map;
        }

        public String[] getLayoutFolders() {
            if (containsKey(LAYOUT_FOLDERS)) {
                return getProperty(LAYOUT_FOLDERS).split(",");
            }
            return new String[]{"layout"};
        }

        public String getProjectPath() {
            return getProperty("PROJECT_PATH");
        }

        public String getModulePath() {
            return getProperty("MODULE_PATH");
        }

        public String getActivitiesInjectorFactory() {
            String filepath = getProperty(ACTIVITY_INJECTOR_FACTORY_FILE);
            if (filepath != null) {
                filepath = filepath.trim();
                if (filepath.startsWith("$PROJECT_DIR$")) {
                    filepath = filepath.replace("$PROJECT_DIR$", getProjectPath());
                } else if (filepath.startsWith("$MODULE_DIR$")) {
                    filepath = filepath.replace("$MODULE_DIR$", getModulePath());
                }
                return filepath;
            } else {
                return null;
            }
        }

        public String getFragmentsInjectorFactory() {
            String filepath = getProperty(FRAGMENT_INJECTOR_FACTORY_FILE);
            if (filepath != null) {
                filepath = filepath.trim();
                if (filepath.startsWith("$PROJECT_DIR$")) {
                    filepath = filepath.replace("$PROJECT_DIR$", getProjectPath());
                } else if (filepath.startsWith("$MODULE_DIR$")) {
                    filepath = filepath.replace("$MODULE_DIR$", getModulePath());
                }
                return filepath;
            } else {
                return null;
            }
        }
    }

}
