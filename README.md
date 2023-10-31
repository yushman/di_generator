# DI Generator
## _AS Plugin thats generate api/impl modules with DI_

## Installation
Add this plugin through Settings -> Plugins -> Gear Icon -> Install from disk

## Usage
Right click on a file or directory in "Project view"
Choose "Generate module and DI" in the bottom of drop-down menu
Fill the dialog fields and press OK

## Result
Generated file tree like:

#### *Api*
- api_module/build.gradle.kts 
- api_module/package/api/di/ApiName.kt

#### *Impl*
- impl_module/build.gradle.kts 
- impl_module/package/impl/di/ApiNameComponent.kt
- impl_module/package/impl/di/ApiNameModule.kt
- impl_module/package/impl/di/ApiNameProviderModule.kt
