import React from 'react';
import './hero.css';

class Hero extends React.Component {
    render() {
        const { children } = this.props;
        return (
            <div className="hero">
                {children}
            </div>
        );
    }
}

export default Hero;