import React from 'react';
import logo from '../assets/logo.svg';

class Home extends React.Component {
    render() {
        return (
            <div className="home">
                <div className="home-header">
                    <img src={logo} className="home-logo" alt="logo" />
                    <h2>Hello React!</h2>
                </div>
            </div>
        );
    }
}

export default Home;