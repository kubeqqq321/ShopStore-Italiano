import { Injectable } from "@angular/core";

export interface Menu{
    state:string;
    name:string;
    type:string;
    icon:string;
    role:string;
}

const MENUITEMS = [
    { state:'dashboard', name: 'Dashboard', type: 'link', icon: 'dashboard', role: ''},
    { state: 'category', name: 'Zarządzaj kategoriami', type: 'link', icon: 'category', role: 'admin'},
    { state: 'product', name: 'Zarządzaj produktami', type: 'link', icon: 'inventory_2', role: 'admin' },
    { state: 'order', name: 'Zarządzaj Zamówieniami', type: 'link', icon: 'shopping_cart', role: '' },
    { state: 'bill', name: 'Zarządzaj Rachunkami', type: 'link', icon: 'backup_table', role: '' },
    { state: 'user', name: 'Zarządzaj Użytkownikiem', type: 'link', icon: 'people', role: 'admin' }
]


@Injectable()

export class MenuItems{
    getMenuitem():Menu[]{
        return MENUITEMS;
    }
}
