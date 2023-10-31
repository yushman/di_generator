package my.awesome.di.generator.forms

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.notificationGroup
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.dsl.builder.whenTextChangedFromUi
import my.awesome.di.generator.utils.Files

class MainDialog(
    anActionEvent: AnActionEvent,
) : DialogWrapper(anActionEvent.project) {
    private val project = anActionEvent.project

    // Получим PSI (Program Structure Interface) элемент из экшена
    private val element = anActionEvent.getData(LangDataKeys.PSI_ELEMENT)

    // Попробуем понять, что мы щелкнули именно на файл или директорию
    private val dir = element as? PsiDirectory
        ?: (element as? PsiFile)?.containingDirectory

    private var moduleName: String = "my_awesome_module"
    private var packageName: String = "my.awesome.module."
    private var apiClassName: String = "MyAwesomeApi"

    init {
        super.init()
    }

    override fun createCenterPanel() = panel {
        row("Enter module name (without _api/_impl)") {
            textField().text("my_awesome_module").whenTextChangedFromUi { moduleName = it }
        }

        row("Enter package (without .api/.impl)") {
            textField().text("com.sdkit.jazz.").whenTextChangedFromUi { packageName = it }
        }
        row("Enter Api class name") {
            textField().text("MyAwesomeApi").whenTextChangedFromUi { apiClassName = it }
        }
    }

    override fun doOKAction() {
        generateFiles()
        super.doOKAction()
    }

    private fun generateFiles() {
        // Уберем лишние точки в пакете
        val trimmedPackage = packageName.trim('.')

        // Получим инстанс фабрики файлов для их дальнейшего создания
        val psiFF = PsiFileFactory.getInstance(project)

        dir ?: return

        // Создадим виртуальные build.gradle файлы и добавим их в соответствующие директории
        WriteCommandAction.writeCommandAction(project).run<Throwable> {
            val apiModuleDir = dir.createSubdirectory("${moduleName}_api")
            val implModuleDir = dir.createSubdirectory("${moduleName}_impl")

            val apiBG = psiFF.createFileFromText(
                "build.gradle.kts",
                FileTypes.PLAIN_TEXT,
                Files.apiBuildGradle(),
            )
            val implBG = psiFF.createFileFromText(
                "build.gradle.kts",
                FileTypes.PLAIN_TEXT,
                Files.implBuildGradle(trimmedPackage, moduleName),
            )
            apiModuleDir.add(apiBG)
            implModuleDir.add(implBG)

            // Создадим основные дирректории -  src/main/kotlin
            var apiDir = apiModuleDir.createSubdirectory("src")
                .createSubdirectory("main")
                .createSubdirectory("kotlin")
            var implDir = implModuleDir.createSubdirectory("src")
                .createSubdirectory("main")
                .createSubdirectory("kotlin")

            // Создадим поддиректории пакета
            val subDirs = trimmedPackage.split(".")
            subDirs.forEach {
                apiDir = apiDir.createSubdirectory(it)
                implDir = implDir.createSubdirectory(it)
            }

            // Создадим директории апи и импл
            apiDir = apiDir.createSubdirectory("api").createSubdirectory("di")
            implDir = implDir.createSubdirectory("impl").createSubdirectory("di")

            // Создадим файл Api
            val api = psiFF.createFileFromText(
                "$apiClassName.kt",
                FileTypes.PLAIN_TEXT,
                Files.api(trimmedPackage, apiClassName)
            )
            apiDir.add(api)

            // Создадим файлы DI
            psiFF.createFileFromText(
                "${apiClassName}Component.kt",
                FileTypes.PLAIN_TEXT,
                Files.component(trimmedPackage, apiClassName)
            ).also { implDir.add(it) }
            psiFF.createFileFromText(
                "${apiClassName}Module.kt",
                FileTypes.PLAIN_TEXT,
                Files.module(trimmedPackage, apiClassName)
            ).also { implDir.add(it) }
            psiFF.createFileFromText(
                "${apiClassName}ProviderModule.kt",
                FileTypes.PLAIN_TEXT,
                Files.apiProviderModule(trimmedPackage, apiClassName)
            ).also { implDir.add(it) }

            // Покажем нотификацию, что всё прошло успешно
            okNotification()
        }
    }

    private fun directoryExistsNotification() {
        notificationGroup.createNotification("Module directories already exist", NotificationType.ERROR).notify(project)
    }

    private fun okNotification() {
        notificationGroup.createNotification("Generated successfully", NotificationType.INFORMATION).notify(project)
    }

    override fun doValidate(): ValidationInfo? {
        fun validationInfo(s: String) = ValidationInfo(s)
        dir ?: return validationInfo("Please choose right directory to generate di")
        try {
            dir.checkCreateSubdirectory("${moduleName}_api")
            dir.checkCreateSubdirectory("${moduleName}_impl")
        } catch (_: Exception) {
            return validationInfo("Module directories already exist")
        }
        return super.doValidate()
    }
}
