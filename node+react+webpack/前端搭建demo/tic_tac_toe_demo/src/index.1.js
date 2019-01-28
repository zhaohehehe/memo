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
function Square(props){
  return (
    <button className='square' onClick={props.onClick}>
      {props.value}
    </button>
  );
}



class Board extends React.Component {
  //要从多个子级收集数据 或 使两个子组件之间相互通信，您需要在其父组件中声明共享 state(状态) 。父组件可以使用props(属性) 将 state(状态) 传递回子节点；这可以使子组件彼此同步并与父组件保持同步。
  constructor() {
    super();
    this.state = {
      squares: Array(9).fill(null),
      xIsNext:true
    }
  }
  handleClick(i){
    //调用 .slice() 来创建 squares 数组的副本来修改，而不是修改现有的数组
    const squares = this.state.squares.slice();
    //如果有人赢了或者当前方格已经被填充时，忽略点击
    if(calculateWinner(squares)||squares[i]){
      return;
    }
    squares[i]=this.state.xIsNext ? 'X' : 'O';
    this.setState({squares:squares,xIsNext:!this.state.xIsNext});
  }
  renderSquare(i) {
    return (
      <Square
        value={this.state.squares[i]}
        onClick={() => this.handleClick(i)}
      />
    );
  }
  render() {
    const winner = calculateWinner(this.state.squares);
    let status;
    if(winner){
      status='Winner:'+winner
    }else{
      status ='Next player: ' + (this.state.xIsNext ? 'X':'O');
    }
    return (
      <div>
        <div className="status">{status}</div>
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
//宣布获胜者
function calculateWinner(squares){
  const lines =[
    [0, 1, 2],
    [3, 4, 5],
    [6, 7, 8],
    [0, 3, 6],
    [1, 4, 7],
    [2, 5, 8],
    [0, 4, 8],
    [2, 4, 6],
  ];
  for(let i=0;i<lines.length;i++){
    const [a,b,c] = lines[i];
    if(squares[a] && squares[a]==squares[b] && squares[a]==squares[c]){
      return squares[a];
    }
  }
  return null;
}


ReactDOM.render(
  <Board />,
  document.getElementById('root')
);