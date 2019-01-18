function Hello() { 
    var name; 
    var age;
    this.setAge = function(theAge){
        age=theAge;
    };
    this.setName = function(thyName) { 
        name = thyName; 
    }; 
    this.sayHello = function() { 
        console.log('Hello ' + name); 
        console.log('hello'+ age);
    }; 
}; 
module.exports = Hello;