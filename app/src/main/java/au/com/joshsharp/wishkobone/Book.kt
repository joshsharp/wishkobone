package au.com.joshsharp.wishkobone

class Book(
    var authors: ArrayList<String>,
    var title: String,
    var imageUrl: String,
    var url: String,
    var series: String?,
    var synopsis: String,
    var price: Float? = 0.0f
){

    fun formattedPrice(): String {
        if (this.price != null){
            return String.format("$%.2f", this.price)
        }
        return "N/A"
    }
}