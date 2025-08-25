import React, { useState, useEffect } from 'react';
import { Button, Form, FormGroup, Input, Label } from 'reactstrap';
import api from '../../services/api';

function TaskForm({ boardId, editingTask, onSuccess }) {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priority: 4,
    deadline: '',
    status: false,
  });

  useEffect(() => {
    if (editingTask) {
      setFormData({
        title: editingTask.title,
        description: editingTask.description || '',
        priority: editingTask.priority,
        deadline: editingTask.deadline || '',
        status: editingTask.status,
      });
    } else {
      setFormData({
        title: '',
        description: '',
        priority: 4,
        deadline: '',
        status: false,
      });
    }
  }, [editingTask]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = { ...formData, taskBoard: { id: boardId } };
      if (editingTask) {
        await api.patch(`/tasks/${editingTask.id}`, payload);
      } else {
        await api.post('/tasks', payload);
      }
      onSuccess();
    } catch (error) {
      console.error('Error saving task:', error);
    }
  };

  return (
    <Form onSubmit={handleSubmit}>
      <FormGroup>
        <Label>Title</Label>
        <Input
          type="text"
          value={formData.title}
          onChange={(e) => setFormData({ ...formData, title: e.target.value })}
          placeholder="Task title"
          required
        />
      </FormGroup>
      <FormGroup>
        <Label>Description</Label>
        <Input
          type="textarea"
          value={formData.description}
          onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          placeholder="Task description"
        />
      </FormGroup>
      <FormGroup>
        <Label>Priority</Label>
        <Input
          type="number"
          value={formData.priority}
          onChange={(e) => setFormData({ ...formData, priority: parseInt(e.target.value) })}
          min="1"
          max="5"
        />
      </FormGroup>
      <FormGroup>
        <Label>Deadline</Label>
        <Input
          type="date"
          value={formData.deadline}
          onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
        />
      </FormGroup>
      <FormGroup check>
        <Label check>
          <Input
            type="checkbox"
            checked={formData.status}
            onChange={(e) => setFormData({ ...formData, status: e.target.checked })}
          />{' '}
          Completed
        </Label>
      </FormGroup>
      <Button color="primary" type="submit">
        {editingTask ? 'Update Task' : 'Create Task'}
      </Button>
    </Form>
  );
}

export default TaskForm;