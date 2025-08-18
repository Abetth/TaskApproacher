// src/components/Auth/UserProfile.jsx
import React, { useContext } from 'react';
import { Card, CardBody, CardTitle } from 'reactstrap';
import { AuthContext } from '../../contexts/AuthContext';

function UserProfile() {
  const { user } = useContext(AuthContext);

  if (!user) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h2>User Profile</h2>
      <Card>
        <CardBody>
          <CardTitle tag="h5">Username: {user.username}</CardTitle>
          <p>Email: {user.email}</p>
          <p>ID: {user.id}</p>
        </CardBody>
      </Card>
    </div>
  );
}

export default UserProfile;