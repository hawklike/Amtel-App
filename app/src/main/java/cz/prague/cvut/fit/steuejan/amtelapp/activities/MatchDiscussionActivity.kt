package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.lifecycle.observe
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.Query.Direction.DESCENDING
import cz.prague.cvut.fit.steuejan.amtelapp.R
import cz.prague.cvut.fit.steuejan.amtelapp.adapters.ShowMessagesPagingAdapter
import cz.prague.cvut.fit.steuejan.amtelapp.business.managers.MessageManager
import cz.prague.cvut.fit.steuejan.amtelapp.data.entities.Message
import cz.prague.cvut.fit.steuejan.amtelapp.view_models.MatchDiscussionActivityVM

class MatchDiscussionActivity : AbstractBaseActivity()
{
    private val viewModel by viewModels<MatchDiscussionActivityVM>()

    private var matchId = ""

    private lateinit var addMessageButton: FloatingActionButton

    private var messagesRecyclerView: RecyclerView? = null
    private var messagesAdapter: ShowMessagesPagingAdapter? = null

    companion object
    {
        const val MATCH_ID = "matchId"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.match_discussion)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.discussion))
        setArrowBack()

        intent.extras?.let { bundle ->
            matchId = bundle.getString(MATCH_ID, "Who is reading this, I wish you a very nice day.")
        }

        addMessage()
        showMessages()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        if(::addMessageButton.isLateinit) addMessageButton.setOnClickListener(null)

        messagesRecyclerView?.adapter = null
        messagesAdapter = null
        messagesRecyclerView = null
    }

    private fun addMessage()
    {
        addMessageButton = findViewById(R.id.match_discussion_add_button)

        addMessageButton.setOnClickListener {
            MaterialDialog(this)
                .title(text = "Přidat příspěvek")
                .show {
                    var input: CharSequence = ""
                    input(hint = "Zde napište příspěvek...", maxLength = 1024, waitForPositiveButton = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        inputField.setSingleLine(false)
                        inputField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                        inputField.maxLines = 4

                        inputField.error =
                            if(text.trim().isNotEmpty()) null
                            else "Příspěvek nesmí být prázdný."

                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, text.trim().isNotEmpty())
                        input = text
                    }
                    positiveButton(text = "Přidat") { viewModel.addMessage(input.toString(), matchId) }
                    negativeButton()
                }
        }

        viewModel.message.observe(this) { sent ->
            if(sent) messagesAdapter?.refresh()
        }
    }

    private fun showMessages()
    {
        messagesRecyclerView = findViewById(R.id.match_discussion_recyclerView)
        messagesRecyclerView?.setHasFixedSize(true)
        messagesRecyclerView?.layoutManager = LinearLayoutManager(this)

        val query = MessageManager.getMessages(matchId, false, DESCENDING)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(4)
            .setPageSize(10)
            .build()

        val options = FirestorePagingOptions.Builder<Message>()
            .setQuery(query, config, Message::class.java)
            .setLifecycleOwner(this)
            .build()

        messagesAdapter = ShowMessagesPagingAdapter(options)
        messagesRecyclerView?.adapter = messagesAdapter
    }
}