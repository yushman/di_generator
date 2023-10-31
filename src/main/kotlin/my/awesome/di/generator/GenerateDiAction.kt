package my.awesome.di.generator

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.notificationGroup
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import my.awesome.di.generator.forms.MainDialog

class GenerateDiAction: AnAction(){
    override fun actionPerformed(e: AnActionEvent) {
        val element = e.getData(LangDataKeys.PSI_ELEMENT)

        // Проверяем, что щелкнули на файл или директорию
        element as? PsiDirectory
            ?: (element as? PsiFile)?.containingDirectory
            ?: return directoryNotification(e)

        // Показываем диалог
        val dialog = MainDialog(e)
        dialog.show()
    }

    private fun directoryNotification(e: AnActionEvent) {
        notificationGroup.createNotification("Please choose right directory to generate di", NotificationType.ERROR)
            .notify(e.project)
    }
}