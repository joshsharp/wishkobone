package au.com.joshsharp.wishkobone

class Book(
    var id: String,
    var authors: ArrayList<String>,
    var title: String,
    var imageUrl: String,
    var url: String,
    var series: String?,
    var synopsis: String,
    var price: Float? = 0.0f,
    var currency: String = "$"
){

    fun formattedPrice(): String {
        if (this.price != null){
            return String.format("%s%.2f", this.currency, this.price)
        }
        return "N/A"
    }
}