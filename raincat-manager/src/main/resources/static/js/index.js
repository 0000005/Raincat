
function switchTransaction()
{
    $.ajax({
        type: "GET",
        url: "/tx/manager/transaction-switch",
        success: function(data){
            if(data=="ok")
            {
                alert("操作成功！")
            }
            else
            {
                alert("操作失败！")
            }
            window.location.reload();
        }
    });
}
