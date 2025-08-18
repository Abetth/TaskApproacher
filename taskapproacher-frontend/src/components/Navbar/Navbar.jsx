import React, { useContext } from 'react';
import { Navbar as BSNavbar, NavbarBrand, Nav, NavItem, NavLink, Button } from 'reactstrap';
import { Link } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';

function Navbar() {
  const { user, logout } = useContext(AuthContext);

  return (
    <BSNavbar color="light" light expand="md">
      <NavbarBrand tag={Link} to="/">Task Planner</NavbarBrand>
      <Nav className="ml-auto" navbar>
        {user ? (
          <>
            <NavItem>
              <NavLink tag={Link} to="/profile">{user.username}</NavLink>
            </NavItem>
            <NavItem>
              <Button color="link" onClick={logout}>Logout</Button>
            </NavItem>
          </>
        ) : (
          <>
            <NavItem>
              <NavLink tag={Link} to="/login">Login</NavLink>
            </NavItem>
            <NavItem>
              <NavLink tag={Link} to="/register">Register</NavLink>
            </NavItem>
          </>
        )}
      </Nav>
    </BSNavbar>
  );
}

export default Navbar;