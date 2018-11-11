package com.example.thanaphoombabparn.firemessage.recyclerView.item

import android.content.Context
import com.example.thanaphoombabparn.firemessage.R
import com.example.thanaphoombabparn.firemessage.glide.GlideApp
import com.example.thanaphoombabparn.firemessage.model.User
import com.example.thanaphoombabparn.firemessage.utility.StorageUtil
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class PersonItem(
    val person: User,
    val userId: String,
    private val context: Context
) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView_name.text = person.name
        viewHolder.textView_bio.text = person.bio
        if(person.profilePicturePath != null)
            GlideApp.with(context)
                .load(StorageUtil.pathToReference(person.profilePicturePath))
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(viewHolder.imageView_profile_picture)
    }

    override fun getLayout(): Int {
        return R.layout.item_person
    }

}