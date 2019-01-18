import React from 'react';
import { observable, action, computed } from 'mobx';
import { observer } from 'mobx-react';
import DevTools, { setLogEnabled } from 'mobx-react-devtools';
import App from './App';

class Store {
    
    /** mobx devtools 位置 */
    positions = [
        { top: 5, right: 20 },
        { bottom: 5, right: 20 },
        { bottom: 5, left: 20 },
        { top: 5, left: 20 }
    ];

    /** 当前位置的index */
    @observable currentPositionIndex = 0;

    /** 计算：当前位置 */
    @computed get currentPosition() {
        return this.positions[this.currentPositionIndex];
    }
    
    /** 下一个位置 */
    @action nextPosition = () => {
        this.currentPositionIndex = (this.currentPositionIndex + 1) % this.positions.length;
    }
}

const store = new Store();

@observer
class Root extends React.Component {

    handleKeydown = (e) => {
        if (e.ctrlKey && e.keyCode === 80) {
            // ctrl + p
            store.nextPosition();
            e.preventDefault();
        }
    }

    componentDidMount() {
        setLogEnabled(true);
        document.addEventListener('keydown', this.handleKeydown);
        console.log('Mobx Devtools is now available. Press key "ctrl-p" to changePosition.');
    }

    componentWillUnmount() {
        document.removeEventListener('keydown', this.handleKeydown);
    }

    render() {
        const { children } = this.props;

        return (
            <div>
                <App>{children}</App>
                <DevTools position={store.currentPosition}/>
            </div>
        );
    }

}

export default Root;