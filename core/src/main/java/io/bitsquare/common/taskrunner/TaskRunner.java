/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.common.taskrunner;

import io.bitsquare.common.handlers.ErrorMessageHandler;
import io.bitsquare.common.handlers.ResultHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRunner<T extends Model> {
    private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);

    private final Queue<Class<? extends Task>> tasks = new LinkedBlockingQueue<>();
    protected final T sharedModel;
    private final ResultHandler resultHandler;
    private final ErrorMessageHandler errorMessageHandler;
    private boolean failed = false;
    private boolean isCanceled;

    private Class<? extends Task> currentTask;

    public TaskRunner(T sharedModel, ResultHandler resultHandler, ErrorMessageHandler errorMessageHandler) {
        this.sharedModel = sharedModel;
        this.resultHandler = resultHandler;
        this.errorMessageHandler = errorMessageHandler;
    }

    public final void addTasks(Class<? extends Task<? extends Model>>... items) {
        List<Class<? extends Task<? extends Model>>> list = Arrays.asList(items);
        tasks.addAll(list);
    }

    public void run() {
        next();
    }

    protected void next() {
        if (!failed && !isCanceled) {
            if (tasks.size() > 0) {
                try {
                    currentTask = tasks.poll();
                    log.trace("Run task: " + currentTask.getSimpleName());
                    log.debug("sharedModel.getClass() " + sharedModel.getClass());
                    log.debug("sharedModel.getClass().getSuperclass() " + sharedModel.getClass().getSuperclass());
                   /* Object c = currentTask.getDeclaredConstructor(TaskRunner.class, sharedModel.getClass());
                    log.debug("c " + c);
                    Object c2 = currentTask.getDeclaredConstructor(TaskRunner.class, sharedModel.getClass().getSuperclass());
                    log.debug("c getSuperclass " + c2);
                    Object o = currentTask.getDeclaredConstructor(TaskRunner.class, sharedModel.getClass()).newInstance(this, sharedModel);*/
                    //TODO solve in tasks problem with superclasses
                    try {
                        currentTask.getDeclaredConstructor(TaskRunner.class, sharedModel.getClass()).newInstance(this, sharedModel).run();
                    } catch (Throwable throwable) {
                        currentTask.getDeclaredConstructor(TaskRunner.class, sharedModel.getClass().getSuperclass()).newInstance(this, sharedModel).run();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    handleErrorMessage("Error at taskRunner: " + throwable.getMessage());
                }
            }
            else {
                resultHandler.handleResult();
            }
        }
    }

    public void cancel() {
        isCanceled = true;
    }

    void handleComplete() {
        log.trace("Task completed: " + currentTask.getSimpleName());
        sharedModel.persist();
        next();
    }

    void handleErrorMessage(String errorMessage) {
        log.error("Task failed: " + currentTask.getSimpleName());
        log.error("errorMessage: " + errorMessage);
        failed = true;
        errorMessageHandler.handleErrorMessage(errorMessage);
    }
}