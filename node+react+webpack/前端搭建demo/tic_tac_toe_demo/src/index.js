import React from 'react';
import ReactDOM from 'react-dom';
/*
class Square extends React.Component {
  //constructor() {
  //  super();//在 JavaScript classes(类)中， 在定义子类的构造函数时，你需要始终调用 super
  //  this.state = {
  //    value: null
  //  }
  //}
  render() {
    return (
      //当你在一个组件中调用 setState 时， React 也会自动更新其子组件
      <button
        className="square"
        onClick={() => this.props.onClick()}
      >
        {this.props.value}
      </button>
    );
  }
}
*/
//函数式组件代替上边的纯组件
function Square(props) {
  return (
    <button className='square' onClick={props.onClick}>
      {props.value}
    </button>
  );
}



class Board extends React.Component {
  renderSquare(i) {
    return (
      <Square
        value={this.props.squares[i]}
        onClick={() => this.props.onClick(i)}
      />
    );
  }
  render() {
    return (
      <div>
        <div className="board-row">
          {this.renderSquare(0)}
          {this.renderSquare(1)}
          {this.renderSquare(2)}
        </div>
        <div className="board-row">
          {this.renderSquare(3)}
          {this.renderSquare(4)}
          {this.renderSquare(5)}
        </div>
        <div className="board-row">
          {this.renderSquare(6)}
          {this.renderSquare(7)}
          {this.renderSquare(8)}
        </div>
      </div>
    );
  }
}

class Game extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      history: [{
        squares: Array(9).fill(null)
      }],
      xIsNext: true,
      stepNumber: 0,
    }
  }
  handleClick(i) {
    //确保了如果我们进行了过程回放，然后从那一步开始新的动作，我们抛弃了所有后续的记录
    const history = this.state.history.slice(0, this.state.stepNumber + 1);
    const current = history[history.length - 1];
    const squares = current.squares.slice();
    //如果有人赢了或者当前方格已经被填充时，忽略点击
    if (calculateWinner(squares) || squares[i]) {
      return;
    }
    squares[i] = this.state.xIsNext ? 'X' : 'O';
    this.setState({
      //与数组 push() 方法不同，concat() 方法不会改变原始数组，所以我们更喜欢它
      history: history.concat([{
        squares: squares
      }]),
      stepNumber: history.length,//确保我们不会在制作新动作后显示相同的动作。
      xIsNext: !this.state.xIsNext,
    });
  }
  jumpTo(step) {
    this.setState({
      stepNumber: step,
      xIsNext: (step % 2) === 0,
    });
  }
  render() {
    const history = this.state.history;
    const current = history[this.state.stepNumber];
    const winner = calculateWinner(current.squares);
    const moves = history.map((step, move) => {
      const desc = move ? 'Go to move#' + move : 'Go to game start';
      return (
        //key 是由 React 保留的特殊属性（以及 ref，更高级的功能）
        <li key={move}>
          <a href="#" onClick={() => this.jumpTo(move)}>{desc}</a>
        </li>
      );
    });
    let status;
    if (winner) {
      status = 'Winner:' + winner
    } else {
      status = 'Next player: ' + (this.state.xIsNext ? 'X' : 'O');
    }
    return (
      <div className="game">
        <div className="game-board">
          <Board
            squares={current.squares}
            onClick={(i) => this.handleClick(i)}
          />
        </div>
        <div className="game-info">
          <div>{status}</div>
          <ol>{moves}</ol>
        </div>
      </div>
    );
  }
}





//宣布获胜者
function calculateWinner(squares) {
  const lines = [
    [0, 1, 2],
    [3, 4, 5],
    [6, 7, 8],
    [0, 3, 6],
    [1, 4, 7],
    [2, 5, 8],
    [0, 4, 8],
    [2, 4, 6],
  ];
  for (let i = 0; i < lines.length; i++) {
    const [a, b, c] = lines[i];
    if (squares[a] && squares[a] === squares[b] && squares[a] === squares[c]) {
      return squares[a];
    }
  }
  return null;
}


ReactDOM.render(
  <Game />,
  document.getElementById('root')
);