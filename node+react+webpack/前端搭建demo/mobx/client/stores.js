import { HeroHeroListStore } from './src/hero/store';

const Stores = {
    HeroHeroListStore
};

export function createStore(state) {
    const rootStore = {};
    
    if (state) {
        Object.keys(Stores).map(key => rootStore[key] = new Stores[key](rootStore, state[key]));
    } else {
        Object.keys(Stores).map(key => rootStore[key] = new Stores[key]());
    }

    return rootStore;
}

