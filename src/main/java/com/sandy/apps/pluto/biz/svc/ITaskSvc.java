/**
 * Creation Date: Aug 3, 2008
 */

package com.sandy.apps.pluto.biz.svc;

import com.sandy.apps.pluto.shared.STException ;
import com.sandy.apps.pluto.shared.dto.TaskDetail ;
import com.sandy.apps.pluto.shared.util.dataaccess.DataAccessException ;

/**
 * An interface which manages registered tasks (jobs) in the application
 *
 * @author Sandeep Deb [deb.sandeep@gmail.com]
 */
public interface ITaskSvc {

    /**
     * A static constant identifying the key against which the task identifier
     * is stored in the scheduled jobs data map.
     */
    String KEY_TASK_ID = "taskId" ;

    /**
     * Adds a task to the persistent storage and starts it if the startup type
     * of the newly added task is mentioned as automatic.
     *
     * @param task The task instance to add
     *
     * @throws STException If an exception is encountered during the storage
     *         operation or if the task could not be started.
     */
    void addTask( final TaskDetail task )
        throws STException ;

    /**
     * Updates the specified task. Prior to updating the task, the task is
     * stopped and then updated. Post update if the task was in a running
     * state or if the modified attributes of the task specifies the task as
     * an automatic startup type an attempt is made to start the task post
     * update.
     *
     * @param task The task instance to update
     *
     * @throws STException If an exception is encountered during the storage
     *         operation or starting the task.
     */
    void updateTask( final TaskDetail task )
        throws STException ;

    /**
     * Updates the specified attribute of the given task identifier.
     *
     * @param taskId The identifier of the task whose attribute is to be updated
     * @param key The task attribute key
     * @param value The task attribute value
     *
     * @throws STException If an exception is encountered during the storage
     *         operation or starting the task.
     */
    void updateTaskAttribute( final Integer taskId, final String key,
                              final String value )
        throws STException ;

    /**
     * Deletes the specified task from the persistent storage. If the task
     * is in a running state prior to deletion an attempt is made to stop the
     * task before deleting it from the persistent storage.
     *
     * @param taskId The identifier of the task instance to delete.
     *
     * @throws DataAccessException If an exception is encountered during the
     *         process of deleting the task to the persistent storage.
     */
    void deleteTask( final Integer taskId )
        throws STException ;

    /**
     * Tries to stop the task, identified by the taskId parameter. This method
     * does not change the startup type of the task, it only tries to stop the
     * stop the task for this instance of the application. If the startup type
     * of the application is mentioned as automatic, it will be automatically
     * started the next time the application is started.
     *
     * @param taskId The identifier of the task
     *
     * @throws STException If an exception is encountered while trying to stop
     *         the task.
     */
    void stopTask( final Integer taskId )
        throws STException ;

    /**
     * Tries to start the task, identified by the taskId parameter. This method
     * does not change the startup type of the task, it only tries to start the
     * stop the task for this instance of the application. If the startup type
     * of the application is mentioned as automatic, it will be automatically
     * started the next time the application is started.
     *
     * @param taskId The identifier of the task
     *
     * @throws STException If an exception is encountered while trying to stop
     *         the task.
     */
    void startTask( final Integer taskId )
        throws STException ;

    /**
     * Tries to stop and start the task, identified by the taskId parameter.
     *
     * @param taskId The identifier of the task
     *
     * @throws STException If an exception is encountered while trying to stop
     *         the task.
     */
    void restartTask( final Integer taskId )
        throws STException ;

    /**
     * Shuts down the scheduler
     */
    void shutdownScheduler() throws STException ;
}
