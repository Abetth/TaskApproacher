import React from 'react';
import { Navbar as BSNavbar, NavbarBrand } from 'reactstrap';
import { Link } from 'react-router-dom';

function Navbar() {
  return (
    <BSNavbar color="light" light expand="md">
      <NavbarBrand tag={Link} to="/">Task Planner</NavbarBrand>
    </BSNavbar>
  );
}

export default Navbar;