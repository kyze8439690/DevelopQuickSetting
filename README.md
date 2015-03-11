#DevelopQuickSetting
-------------

<img src="https://raw.githubusercontent.com/kyze8439690/DevelopQuickSetting/master/screenshot.png"  width="270" height="480">

####What can it do?

It provides a simple UI to toggle the developer setting we used the most. You can also place a widget on your launcher, you can use it to toggle setting with a minimum of time. It provides function below:  

- Show Layout Border
- Display Overdraw
- Profile GPU Rendering(Not available on android 4.2)
- Immediately destroy activities
- Adb through WIFI

####Why I develop this application?  

This app is for those who develop android application. If you want to toggle developer settings, you have to go to setting -> developer setting -> fling a long list and choose the item you wanted to toggle. This cause us so much time! 

####Requirement

- Android 4.2+ device
- Root permission

####Why it require root permission?

- In order to write system setting, this application must be install under [system/app] to get system permission.
- When toggle system settings, this application need root permission to run some command.

At last, this app is opensource on [https://github.com/kyze8439690/DevelopQuickSetting](https://github.com/kyze8439690/DevelopQuickSetting).

Author: YangHui <me@yanghui.name>



### Tested device info  
- Nexus 5 running CM12 (android 5.0.2)
- Nexus 5 running CM11 (android 4.4.4)
- Meizu MX2 running Flyme 4.1.2 (android 4.4.4)
- Nubia NX403A running android 4.2.2
- Redmi Note running MIUI V5 (android 4.4.4)