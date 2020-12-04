The document describes the guidelines for the frontend development. 

## TypeScript

* Avoid `any` type
* Use ES6 classes when you want to do use type checking or you want to create instances with `new` or `Object.create`, in other cases use interfaces.
* Learn the [Do's and Don'ts](https://www.typescriptlang.org/docs/handbook/declaration-files/do-s-and-don-ts.html) styleguide

## Code structure 

* Follow the [Angular Style Guide](https://angular.io/guide/styleguide) for naming and structure.
* When computations or complex mappings are needed, try to use pure functions. They are easier to test. 
* Components have to be dumb and contain mainly the representation logic. As soon as you see that one of the methods of your component performs mutations/conversions/preprocessing of the data, consider to put that into a service. 

## CSS and HTML

* Use bootstrap classes wherever possible. 
* Use CSS classes like to [BEM methodology](http://getbem.com/naming/). Except use single hyphen to separate.
* If you have a lot of html attributes, stack them. For example, this is less readable: 

```html

<a [routerLink]="" *ngIf="articles.length < totalItems" (click)="onClickReadMore()"
       class="btn btn-primary btn-lg active" role="button" aria-pressed="true">{{'news.list.more' | translate}}
    </a>
```
and this is more readable: 
```html
<a [routerLink]=""
   *ngIf="articles.length < totalItems" 
   (click)="onClickReadMore()"
   class="btn btn-primary btn-lg active"
   role="button"
   aria-pressed="true">
   {{'news.list.more' | translate}}
</a>
```  
  
