package my.awesome.di.generator.utils

object Files {
    fun apiBuildGradle() = """
        plugins {
            id("convention.kotlin-feature-api")
        }

        group = "my.awesome.app"

        dependencies {
            // Place it here
        }
    """.trimIndent()

    fun implBuildGradle(packageName: String, moduleName: String) = """
        plugins {
            id("convention.android-feature-impl")
        }

        group = "my.awesome.app"
        
        android {
            namespace = "$packageName.impl"
        }

        dependencies {
            api(project(":${moduleName}_api"))

            // Place it here
        }
    """.trimIndent()

    fun api(packageName: String, apiClassName: String) = """
        package $packageName.api.di

        import my.awesome.app.core.di.Api

        /**
         * Апи TODO
         */
        interface $apiClassName : Api {
            // TODO
        }

    """.trimIndent()

    fun component(packageName: String, apiClassName: String) = """
        package $packageName.impl.di

        import my.awesome.app.core.di.getApi
        import my.awesome.app.core.di..scopes.ProjectScope
        import $packageName.api.di.$apiClassName
        import dagger.Component

        /**
         * Компонент Апи [$apiClassName]
         */
        @Component(
            modules = [ ${apiClassName}Module::class ],
            dependencies = [
                // Place it here
            ]
        )
        @ProjectScope
        interface ${apiClassName}Component : $apiClassName {
            @Component.Factory
            interface Factory {
                fun create(
                    corePlatformApi: CorePlatformApi,
                ): $apiClassName
            }

            companion object {
                fun create(): $apiClassName =
                    Dagger${apiClassName}Component.factory().create(
                        corePlatformApi = getApi(),
                    )
            }
        }
    """.trimIndent()

    fun module(packageName: String, apiClassName: String) = """
        package $packageName.impl.di

        import $packageName.api.di.$apiClassName
        import my.awesome.app.core.di.scopes.ProjectScope
        import dagger.Module
        import dagger.Provides

        /**
         * Модуль даггера, который провайдит классы для [$apiClassName]
         */
        @Module
        internal object ${apiClassName}Module {

            @Provides
            @ProjectScope
            fun provideMyAwesomeClass(){}
        }
    """.trimIndent()

    fun apiProviderModule(packageName: String, apiClassName: String) = """
        package $packageName.impl.di

        import $packageName.api.di.$apiClassName
        import my.awesome.app.core.di.ApiKey
        import my.awesome.app.core.di.ApiProvider
        import dagger.Module
        import dagger.Provides
        import dagger.multibindings.IntoMap

        /**
         * Модуль, который провайдит [$apiClassName] в основной граф
         */
        @Module
        object ${apiClassName}ProviderModule {
            @Provides
            @IntoMap
            @ApiKey($apiClassName::class)
            fun provide$apiClassName() =
                ApiProvider { ${apiClassName}Component.create() }
        }
    """.trimIndent()
}