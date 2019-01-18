import { observable, action, useStrict, runInAction } from 'mobx';
import { http, HERO_ADD_HERO, HERO_GET_HEROS } from '../../utils';
useStrict(true);

export class HeroHeroListStore {
    @observable heros = [];
    @observable newHero = {};

    constructor(rootStore, initialState) {
        Object.assign(this, initialState);
        // Object.assign(this, initialState, { rootStore }); 如果在本Store中使用到其他Store，就将rootStore绑定进来
    }

    @action changeNewHero = (name, value) => {
        this.newHero[name] = value;
    }

    @action addHero = async () => {
        const { heros, newHero } = this;
        if (newHero.name) {
            try {
                const hero = await http.post(HERO_ADD_HERO, newHero);
                runInAction(() => {
                    this.heros.push(hero);
                });
            } catch (e) {
                console.log(e);
            }
        } else {
            alert('请输入姓名');
        }
    }

    /** 获取数据 */
    @action loadData = async () => {
        try {
            const heros = await http.get(HERO_GET_HEROS);
            runInAction(() => {
                this.heros = heros;
            });
        } catch (e) {
            console.log(e);
        }
    }
}