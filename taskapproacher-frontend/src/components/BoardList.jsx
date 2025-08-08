import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardBody, CardTitle, Button, Row, Col, Input, ListGroup, ListGroupItem } from 'reactstrap';

function BoardList({ boards, fetchBoards }) {
  const [newBoardTitle, setNewBoardTitle] = useState('');

  const createBoard = async () => {
    if (!newBoardTitle) return;
    try {
      const response = await fetch('/api/boards', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: newBoardTitle, isSorted: false }),
      });
      if (!response.ok) throw new Error('Failed to create board');
      setNewBoardTitle('');
      fetchBoards();
    } catch (error) {
      console.error('Error creating board:', error);
    }
  };

  const deleteBoard = async (boardId) => {
    try {
      const response = await fetch(`/api/boards/${boardId}`, {
        method: 'DELETE',
      });
      if (!response.ok) throw new Error('Failed to delete board');
      fetchBoards();
    } catch (error) {
      console.error('Error deleting board:', error);
    }
  };

  return (
    <div>
      <h2 className="mb-4">All Task Boards</h2>
      <Row className="mb-4">
        <Col md="6">
          <Input
            type="text"
            value={newBoardTitle}
            onChange={(e) => setNewBoardTitle(e.target.value)}
            placeholder="New board title"
          />
        </Col>
        <Col md="2">
          <Button color="primary" onClick={createBoard}>Create Board</Button>
        </Col>
      </Row>
      {boards.length === 0 ? (
        <p>No boards available. Create a new board.</p>
      ) : (
        <Row>
          {boards.map((board) => (
            <Col md="6" key={board.id} className="mb-4">
              <Card>
                <CardBody>
                  <CardTitle tag="h5">
                    <Link to={`/board/${board.id}`}>{board.title}</Link>
                  </CardTitle>
                  <p>
                    Sorting: {board.isSorted ? 'Enabled' : 'Disabled'}
                    <br />
                    Tasks: {board.tasks ? board.tasks.length : 0}
                  </p>
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
                    className="mt-3 me-2"
                  >
                    Delete Board
                  </Button>
                  <Button
                    color="info"
                    size="sm"
                    tag={Link}
                    to={`/board/${board.id}/update`}
                    className="mt-3"
                  >
                    Update Board
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