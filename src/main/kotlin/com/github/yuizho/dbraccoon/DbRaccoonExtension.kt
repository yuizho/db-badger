package com.github.yuizho.dbraccoon

import com.github.yuizho.dbraccoon.annotation.DataSet
import com.github.yuizho.dbraccoon.operation.TypeByColumn
import com.github.yuizho.dbraccoon.processor.createDeleteQueryOperator
import com.github.yuizho.dbraccoon.processor.createInsertQueryOperator
import com.github.yuizho.dbraccoon.processor.createScanQuerySources
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import javax.sql.DataSource

class DbRaccoonExtension @JvmOverloads constructor(
        private val dataSource: DataSource,
        private val cleanupPhase: CleanupPhase = CleanupPhase.BeforeAndAfterTest
) : BeforeTestExecutionCallback, AfterTestExecutionCallback {
    companion object {
        const val COLUMN_BY_TABLE = "columnByTable"
        val logger: Logger = LoggerFactory.getLogger(DbRaccoonExtension::class.java)
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        // When @DataSet is neither applied to Method nor Class, do nothing
        val testMethod = context.requiredTestMethod
        val dataSet = testMethod.getAnnotationFromMethodOrClass(DataSet::class.java) ?: return

        logger.info("start test data preparation before test execution")
        dataSource.connection.use { conn ->
            val columnByTable = dataSet.createScanQuerySources().map { (name, scanner) ->
                name to scanner.scanColumnTypes(conn)
            }.toMap()
            getStore(context).put(COLUMN_BY_TABLE, columnByTable)

            if (cleanupPhase.shouldCleanupBeforeTestExecution) {
                dataSet.createDeleteQueryOperator(columnByTable).executeQueries(conn)
            }
            dataSet.createInsertQueryOperator(columnByTable).executeQueries(conn)
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        // When @DataSet is neither applied to Method nor Class, do nothing
        val testMethod = context.requiredTestMethod
        val dataSet = testMethod.getAnnotationFromMethodOrClass(DataSet::class.java) ?: return
        if (!cleanupPhase.shouldCleanupAfterTestExecution) {
            return
        }
        dataSource.connection.use { conn ->
            logger.info("start test data cleanup after test execution")
            val columnByTable = getStore(context).remove(COLUMN_BY_TABLE) as Map<String, TypeByColumn>
            dataSet.createDeleteQueryOperator(columnByTable).executeQueries(conn)
        }
    }

    private fun getStore(context: ExtensionContext): ExtensionContext.Store {
        return context.getStore(ExtensionContext.Namespace.create(javaClass, context.requiredTestMethod))
    }

    private fun <T : Annotation> Method.getAnnotationFromMethodOrClass(annotationClass: Class<T>): T? {
        return getAnnotation(annotationClass)
                ?: declaringClass.getAnnotation(annotationClass)
    }
}