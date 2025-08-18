// src/components/Boards/BoardList.jsx
import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardBody, CardTitle, Button, Row, Col, Input, FormGroup, Label, ListGroup, ListGroupItem } from 'reactstrap';
import api from '../../services/api';
import { AuthContext } from '../../contexts/AuthContext';

function BoardList() {
  const { user } = useContext(AuthContext);
  const [boards, setBoards] = useState([]);
  const [newBoardTitle, setNewBoardTitle] = useState('');
  const [newBoardIsSorted, setNewBoardIsSorted] = useState(false);

  useEffect(() => {
    if (user) {
      fetchBoards();
    }
  }, [user]);

  const fetchBoards = async () => {
    try {
      const res = await api.get(`/users/${user.id}/boards`);
      setBoards(res.data);
    } catch (error) {
      console.error('Error fetching boards:', error);
    }
  };

  const createBoard = async () => {
    if (!newBoardTitle) return;
    try {
      await api.post('/boards', { title: newBoardTitle, isSorted: newBoardIsSorted, user: { id: user.id } });
      setNewBoardTitle('');
      setNewBoardIsSorted(false);
      fetchBoards();
    } catch (error) {
      console.error('Error creating board:', error);
    }
  };

  const deleteBoard = async (boardId) => {
    try {
      await api.delete(`/boards/${boardId}`);
      fetchBoards();
    } catch (error) {
      console.error('Error deleting board:', error);
    }
  };

  return (
    <div>
      <h2 className="mb-4">All Task Boards</h2>
      <Row className="mb-4">
        <Col md="4">
          <FormGroup>
            <Label>Title</Label>
            <Input
              type="text"
              value={newBoardTitle}
              onChange={(e) => setNewBoardTitle(e.target.value)}
              placeholder="New board title"
            />
          </FormGroup>
        </Col>
        <Col md="4">
          <FormGroup>
            <Label>Sorting</Label>
            <Input
              type="select"
              value={newBoardIsSorted}
              onChange={(e) => setNewBoardIsSorted(e.target.value === 'true')}
            >
              <option value={false}>Disabled</option>
              <option value={true}>Enabled</option>
            </Input>
          </FormGroup>
        </Col>
        <Col md="2" className="align-self-end">
          <Button color="primary" onClick={createBoard}>Create Board</Button>
        </Col>
      </Row>
      {boards.length === 0 ? (
        <p>No boards available. Create a new board.</p>
      ) : (
        <Row>
          {boards.map((board) => (
            <Col md="4" key={board.id} className="mb-4">
              <Card>
                <CardBody>
                  <CardTitle tag="h5">
                    <Link to={`/board/${board.id}`}>{board.title}</Link>
                  </CardTitle>
                  <p>Sorting: {board.sorted ? 'Enabled' : 'Disabled'}</p>
                  {board.tasks && board.tasks.length > 0 ? (
                    <ListGroup className="mt-3">
                      {board.tasks.map((task) => (
                        <ListGroupItem key={task.id}>
                          <strong>{task.title}</strong>
                          <br />
                          Description: {task.description || 'No description'}
                          <br />
                          Priority: {task.priority}
                          <br />
                          Deadline: {task.deadline}
                          <br />
                          Status: {task.status ? 'Completed' : 'Pending'}
                        </ListGroupItem>
                      ))}
                    </ListGroup>
                  ) : (
                    <p>No tasks available</p>
                  )}
                  <Button
                    color="danger"
                    size="sm"
                    onClick={() => deleteBoard(board.id)}
                    className="me-2"
                  >
                    Delete Board
                  </Button>
                  <Button
                    color="info"
                    size="sm"
                    tag={Link}
                    to={`/board/${board.id}/edit`}
                  >
                    Edit Board
                  </Button>
                </CardBody>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
}

export default BoardList;