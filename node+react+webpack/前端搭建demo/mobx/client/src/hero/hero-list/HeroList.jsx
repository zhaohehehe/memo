import React from 'react';
import { observer, inject } from 'mobx-react';
import style from './heroList.m.css';


@inject('HeroHeroListStore') @observer
class HeroList extends React.Component {

    componentDidMount() {
        const { loadData } = this.props.HeroHeroListStore;
        loadData();
    }

    render() {
        const { heros, changeNewHero, addHero } = this.props.HeroHeroListStore;
        return (
            <div className="hero-list">
                <div className="add-hero">
                    <p><label>姓名：</label><input type="text" onChange={e => changeNewHero('name', e.target.value)} /></p>
                    <p><label>描述：</label><input type="text" onChange={e => changeNewHero('desc', e.target.value)} /></p>
                    <p><button onClick={addHero}>添加</button></p>
                </div>
                <div>
                    <p className={style.title}>英雄列表</p>
                    <ul className="list">
                        {heros.map(hero => <ui key={hero.id}><span>{hero.id}</span>{hero.name}</ui>)}
                    </ul>
                </div>
            </div>
        );
    }
}

export default HeroList;