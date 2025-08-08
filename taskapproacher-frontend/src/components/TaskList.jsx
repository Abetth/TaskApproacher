import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardBody, CardTitle, Button, Row, Col, Input, Label } from 'reactstrap';

function TaskList() {
  const { boardId } = useParams();
  const [tasks, setTasks] = useState([]);
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    priority: 4,
    deadline: '',
    status: false,
  });

  useEffect(() => {
    fetchTasks();
  }, [boardId]);

  const fetchTasks = async () => {
    try {
      const response = await fetch(`/api/boards/${boardId}/tasks`);
      if (!response.ok) throw new Error('Failed to fetch tasks');
      const data = await response.json();
      setTasks(data);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    }
  };

  const createTask = async () => {
    try {
      const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...newTask, taskBoard: { id: boardId } }),
      });
      if (!response.ok) throw new Error('Failed to create task');
      setNewTask({ title: '', description: '', priority: 4, deadline: '', status: false });
      fetchTasks();
    } catch (error) {
      console.error('Error creating task:', error);
    }
  };

  const updateTask = async (taskId) => {
    try {
      const response = await fetch(`/api/tasks`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          id: taskId,
          title: newTask.title,
          description: newTask.description,
          priority: newTask.priority,
          deadline: newTask.deadline,
          status: newTask.status,
          taskBoard: { id: boardId },
        }),
      });
      if (!response.ok) throw new Error('Failed to update task');
      setNewTask({ title: '', description: '', priority: 1, deadline: '', status: false });
      fetchTasks();
    } catch (error) {
      console.error('Error updating task:', error);
    }
  };

  const deleteTask = async (taskId) => {
    try {
      const response = await fetch(`/api/tasks/${taskId}`, {
        method: 'DELETE',
      });
      if (!response.ok) throw new Error('Failed to delete task');
      fetchTasks();
    } catch (error) {
      console.error('Error deleting task:', error);
    }
  };

  return (
    <div>
      <h2>Tasks</h2>
      <Row className="mb-3">
        <Col md="8">
          <Label>Title</Label>
          <Input
            type="text"
            value={newTask.title}
            onChange={(e) => setNewTask({ ...newTask, title: e.target.value })}
            placeholder="New task title"
          />
          <Label className="mt-2">Description</Label>
          <Input
            type="textarea"
            value={newTask.description}
            onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
            placeholder="Task description"
          />
          <Label className="mt-2">Priority</Label>
          <Input
            type="number"
            value={newTask.priority}
            onChange={(e) => setNewTask({ ...newTask, priority: parseInt(e.target.value) })}
          />
          <Label className="mt-2">Deadline</Label>
          <Input
            type="date"
            value={newTask.deadline}
            onChange={(e) => setNewTask({ ...newTask, deadline: e.target.value })}
          />
          <Button color="primary" onClick={createTask} className="mt-3">
            Create Task
          </Button>
        </Col>
      </Row>
      <Row>
        {tasks.map((task) => (
          <Col md="4" key={task.id} className="mb-3">
            <Card>
              <CardBody>
                <CardTitle tag="h5">{task.title}</CardTitle>
                <p>{task.description || 'No description'}</p>
                <p>Priority: {task.priority}</p>
                <p>Deadline: {task.deadline}</p>
                <p>Status: {task.status ? 'Completed' : 'Pending'}</p>
                <Button color="danger" size="sm" onClick={() => deleteTask(task.id)} className="mt-3 me-2">
                  Delete
                </Button>
                <Button color='info' size='sm'  onClick={() => updateTask(task.id)} className="mt-3">
                  Update
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