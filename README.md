
# react-native-background-record

## Getting started

`$ npm install react-native-background-record --save`

### Mostly automatic installation

`$ react-native link react-native-background-record`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-background-record` and add `RNBackgroundRecord.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNBackgroundRecord.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import ai.raondata.backgroundrecord.RNBackgroundRecordPackage;` to the imports at the top of the file
  - Add `new RNBackgroundRecordPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-background-record'
  	project(':react-native-background-record').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-background-record/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-background-record')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNBackgroundRecord.sln` in `node_modules/react-native-background-record/windows/RNBackgroundRecord.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Background.Record.RNBackgroundRecord;` to the usings at the top of the file
  - Add `new RNBackgroundRecordPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNBackgroundRecord from 'react-native-background-record';

// TODO: What to do with the module?
RNBackgroundRecord;
```
  