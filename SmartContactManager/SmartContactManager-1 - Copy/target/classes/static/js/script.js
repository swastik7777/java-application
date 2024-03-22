const toggleSidebar = () => {
    console.log("enter");
    if ($(".sidebar").is(":visible")) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

//serach function in serach contact page

const serach =()=>{
  
    let query=$("#search-contact").val();

    if(query=='')
    {
        $(".serach-result").hide();
    }
    else{
       
        //sending request to server
        let url=`http://localhost:8080/serach/${query}`;
        fetch(url).then((response)=>{
            return response.json();
        }).then((data)=>{
           
            
            let text=`<div class='list-group'>`;
            data.forEach((contact)=>{
                text+=`<a href='/user/${contact.cId}/contact' class=list-group-item list-group-action'>${contact.name}</a>`
            })

            text+=`</div>`
            $(".serach-result").html(text);
            $(".serach-result").show();
        })
        
    }
}


const serach_name =()=>{
  
    let query=$("#search-contact-name").val();

 
       
        //sending request to server
        let url="http://localhost:8080/user/serach_name";
    
       
    
}

