package cz.prague.cvut.fit.steuejan.amtelapp.activities

import android.os.Build
import android.os.Bundle
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.widget.RelativeLayout
import android.widget.TextView
import cz.prague.cvut.fit.steuejan.amtelapp.App
import cz.prague.cvut.fit.steuejan.amtelapp.R


class PlayOffActivity : AbstractBaseActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        setContentView(R.layout.playoff)
        super.onCreate(savedInstanceState)
        setToolbarTitle(getString(R.string.playOff))
        setArrowBack()
        showRules()
    }

    private fun showRules()
    {
        val title: TextView = findViewById(R.id.round_choose_week_text)
        title.text = getString(R.string.rules)

        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.marginStart = App.convertDpToPx(12f)
        params.marginEnd = App.convertDpToPx(12f)
        params.topMargin = App.convertDpToPx(36f)
        params.bottomMargin = App.convertDpToPx(8f)

        val rules: TextView = findViewById(R.id.round_deadline_text)
        rules.layoutParams = params

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            rules.justificationMode = JUSTIFICATION_MODE_INTER_WORD

        rules.text = getString(R.string.rules_text)
    }
}