# APK Parser [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jaredrummler/apk-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jaredrummler/apk-parser) [![Software License](https://img.shields.io/badge/license-BSD%203%20Clause-blue.svg)](LICENSE.txt) [![Twitter Follow](https://img.shields.io/twitter/follow/jrummy16.svg?style=social)](https://twitter.com/jrummy16)

#### Features
* Retrieve basic apk metas, such as title, icon, package name, version, etc.
* Parse and convert binary xml file to text 
* Classes from dex file
* Get certificate metas and verify apk signature

![](sample/graphics/apk_parser_sample.png)

#### Get apk-parser
Download [the latest AAR](https://repo1.maven.org/maven2/com/jaredrummler/apk-parser/1.0.2/apk-parser-1.0.2.aar) or grab via Gradle:

```groovy
compile 'com.jaredrummler:apk-parser:1.0.2'
```

#### Usage
The easiest way is to use the ApkParser class, which contains convenient methods to get AndroidManifest.xml, apk meta infos, etc.
#####1. Apk meta info
ApkMeta contains name(label), packageName, version, sdk, used features, etc.
```java
PackageManager pm = getPackageManager();
ApplicationInfo appInfo = pm.getApplicationInfo("com.facebook.katana", 0);
ApkParser apkParser = ApkParser.create(appInfo);
ApkMeta meta = apkParser.getApkMeta();
String packageName = meta.packageName;
long versionCode = meta.versionCode;
List<UseFeature> usesFeatures = meta.usesFeatures;
List<String> requestedPermissions = meta.usesPermissions;
```
#####2. Get binary xml and manifest xml file
```java
ApplicationInfo appInfo = getPackageManager().getApplicationInfo("some.package.name", 0);
ApkParser apkParser = ApkParser.create(appInfo);
String readableAndroidManifest = apkParser.getManifestXml();
String xml = apkParser.transBinaryXml("res/layout/activity_main.xml");
```
#####3. Get dex classes
```java
ApplicationInfo appInfo = getPackageManager().getApplicationInfo("com.instagram.android", 0);
ApkParser apkParser = ApkParser.create(appInfo);
List<DexInfo> dexFiles = apkParser.getDexInfos(); // if size > 1 then app is using multidex
for (DexInfo dexInfo : dexFiles) {
  DexClass[] dexClasses = dexInfo.classes;
  DexHeader dexHeader = dexInfo.header;
}
```

#####4. Get certificate and verify apk signature
```java
ApplicationInfo appInfo = getPackageManager().getApplicationInfo("com.instagram.android", 0);
ApkParser apkParser = ApkParser.create(appInfo);
if (apkParser.verifyApk() == ApkParser.ApkSignStatus.SIGNED) {
  System.out.println(apkParser.getCertificateMeta().signAlgorithm);
}
```

#####5. Get intent-filters from apk manifest:
```java
ApkParser parser = ApkParser.create(getPackageManager(), "com.android.settings");
AndroidManifest androidManifest = parser.getAndroidManifest();
for (AndroidComponent component : androidManifest.getComponents()) {
  if (!component.intentFilters.isEmpty()) {
    for (IntentFilter intentFilter : component.intentFilters) {
      // Got an intent filter for activity/service/provider/receiver.
    }
  }
}
```

#####6. Locales
Apk may return different infos(title, icon, etc.) for different region and language, which is 
determined by Locales.
If the locale is not set, the "en_US" locale(<code>Locale.US</code>) is used. You can set the 
locale like this:
```java
ApkParser apkParser = ApkParser.create(filePath);
apkParser.setPreferredLocale(Locale.SIMPLIFIED_CHINESE);
ApkMeta apkMeta = apkParser.getApkMeta();
```
The PreferredLocale parameter work for getApkMeta, getManifestXml, and other binary xmls.
Apk parser will find best match languages with locale you specified.

If locale is set to null, ApkParser will not translate resource tag, just give the resource id.
For example, apk title will be '@string/app_name' instead of 'WeChat'.

___

APK Parser is based on [CaoQianLi's apk-parser](https://github.com/CaoQianLi/apk-parser)
you can download apk here [apk](https://github.com/weiyitai/APKParser/releases/download/v1.0/sample-debug.apk)
