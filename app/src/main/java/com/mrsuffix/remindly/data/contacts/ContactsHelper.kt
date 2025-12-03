package com.mrsuffix.remindly.data.contacts

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventCategory
import com.mrsuffix.remindly.domain.model.EventType
import com.mrsuffix.remindly.domain.model.RepeatType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

data class ContactWithBirthday(
    val id: String,
    val name: String,
    val birthday: LocalDate,
    val photoUri: String? = null,
    var isSelected: Boolean = true
)

@Singleton
class ContactsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun getContactsWithBirthdays(): List<ContactWithBirthday> {
        val contacts = mutableListOf<ContactWithBirthday>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.CommonDataKinds.Event.TYPE,
            ContactsContract.Data.PHOTO_URI
        )
        
        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )
        
        try {
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                ContactsContract.Data.DISPLAY_NAME
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                val birthdayIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                val photoIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIndex) ?: continue
                    val name = cursor.getString(nameIndex) ?: continue
                    val birthdayStr = cursor.getString(birthdayIndex) ?: continue
                    val photoUri = if (photoIndex >= 0) cursor.getString(photoIndex) else null
                    
                    val birthday = parseBirthday(birthdayStr)
                    if (birthday != null) {
                        contacts.add(
                            ContactWithBirthday(
                                id = id,
                                name = name,
                                birthday = birthday,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return contacts.distinctBy { it.id }
    }
    
    private fun parseBirthday(dateStr: String): LocalDate? {
        val formats = listOf(
            "yyyy-MM-dd",
            "--MM-dd",
            "MM-dd",
            "yyyy/MM/dd",
            "dd/MM/yyyy",
            "dd.MM.yyyy",
            "yyyy.MM.dd"
        )
        
        for (format in formats) {
            try {
                return if (format == "--MM-dd" || format == "MM-dd") {
                    val cleanDate = dateStr.replace("--", "")
                    val formatter = DateTimeFormatter.ofPattern("MM-dd")
                    val monthDay = java.time.MonthDay.parse(cleanDate, formatter)
                    LocalDate.of(2000, monthDay.month, monthDay.dayOfMonth)
                } else {
                    LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format))
                }
            } catch (e: DateTimeParseException) {
                continue
            }
        }
        return null
    }
    
    fun contactToEvent(contact: ContactWithBirthday): Event {
        return Event(
            name = contact.name,
            date = contact.birthday,
            eventType = EventType.BIRTHDAY,
            eventCategory = EventCategory.BIRTHDAY,
            repeatType = RepeatType.YEARLY,
            reminderDays = listOf(1, 7),
            note = "",
            isActive = true
        )
    }
}
