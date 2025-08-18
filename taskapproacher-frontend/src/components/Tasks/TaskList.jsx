import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardBody, CardTitle, Button, Row, Col } from 'reactstrap';
import api from '../../services/api';
import TaskForm from './TaskForm';

function TaskList() {
  const { boardId } = useParams();
  const [tasks, setTasks] = useState([]);
  const [editingTask, setEditingTask] = useState(null);

  useEffect(() => {
    fetchTasks();
  }, [boardId]);

  const fetchTasks = async () => {
    try {
      const res = await api.get(`/boards/${boardId}/tasks`);
      setTasks(res.data);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    }
  };

  const handleCreateOrUpdate = () => {
    setEditingTask(null);
    fetchTasks();
  };

  const handleEdit = (task) => {
    setEditingTask(task);
  };

  const deleteTask = async (taskId) => {
    try {
      await api.delete(`/tasks/${taskId}`);
      fetchTasks();
    } catch (error) {
      console.error('Error deleting task:', error);
    }
  };

  return (
    <div>
      <h2>Tasks for Board</h2>
      <TaskForm boardId={boardId} editingTask={editingTask} onSuccess={handleCreateOrUpdate} />
      <Row className="mt-4">
        {tasks.map((task) => (
          <Col md="4" key={task.id} className="mb-3">
            <Card>
              <CardBody>
                <CardTitle tag="h5">{task.title}</CardTitle>
                <p>Description: {task.description || 'No description'}</p>
                <p>Priority: {task.priority}</p>
                <p>Deadline: {task.deadline}</p>
                <p>Status: {task.status ? 'Completed' : 'Pending'}</p>
                <Button color="danger" size="sm" onClick={() => deleteTask(task.id)} className="me-2">
                  Delete
                </Button>
                <Button color="info" size="sm" onClick={() => handleEdit(task)}>
                  Edit
                </Button>
              </CardBody>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}

export default TaskList;