package com.winlator;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

/**
 * A0E3 game profile screen. Uses platform/AppCompat widgets only because the
 * host application intentionally uses an AppCompat theme, not a
 * Theme.MaterialComponents theme.
 */
public final class Aoe3HomeFragment extends Fragment {
    public interface HostActions {
        void onChooseGameFolder();
        void onLaunchGame();
        void onOpenContainers();
        void onOpenGraphicsSettings();
        void onOpenInputSettings();
    }

    private HostActions actions;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HostActions) actions = (HostActions) context;
    }

    @Override
    public void onDetach() {
        actions = null;
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        int pad = dp(context, 20);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(context);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(pad, pad, pad, pad);
        scroll.addView(page);

        page.addView(text(context, "A0E3  /  GAME LIBRARY", 12, 0xFF8EA5C8, true));

        TextView heading = text(context, "Your games.\nOne place.", 30, 0xFFF4F6FA, true);
        LinearLayout.LayoutParams headingParams = params(-1, -2);
        headingParams.topMargin = dp(context, 8);
        page.addView(heading, headingParams);

        TextView intro = text(context,
            "A focused Android front end powered by Winlator's existing Wine, Box64 and graphics stack.",
            14, 0xFFB7C1D1, false);
        LinearLayout.LayoutParams introParams = params(-1, -2);
        introParams.topMargin = dp(context, 8);
        page.addView(intro, introParams);

        LinearLayout gameCard = card(context);
        LinearLayout gameContent = column(context, dp(context, 18));
        gameCard.addView(gameContent);

        gameContent.addView(text(context, "PC GAME PROFILE", 11, 0xFF9FB7DA, true));
        gameContent.addView(text(context, "Age of Empires III", 23, 0xFFFFFFFF, true));
        gameContent.addView(text(context, "DEFINITIVE EDITION", 12, 0xFFB7C1D1, true));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String executablePath = prefs.getString("aoe3_executable_path", null);
        boolean imported = executablePath != null && new java.io.File(executablePath).isFile();
        TextView status = text(context,
            imported ? "Imported · 64-bit executable detected" : "Not installed yet", 13,
            imported ? 0xFF9FE0B0 : 0xFFFFD18A, true);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(context, 14);
        gameContent.addView(status, statusParams);

        TextView note = text(context,
            "Choose your existing game installation folder. The app will not download or include game files.",
            13, 0xFFB7C1D1, false);
        LinearLayout.LayoutParams noteParams = params(-1, -2);
        noteParams.topMargin = dp(context, 6);
        gameContent.addView(note, noteParams);

        Button choose = actionButton(context, "SELECT GAME FOLDER", true);
        choose.setOnClickListener(v -> {
            if (actions != null) actions.onChooseGameFolder();
        });
        LinearLayout.LayoutParams chooseParams = params(-1, -2);
        chooseParams.topMargin = dp(context, 16);
        gameContent.addView(choose, chooseParams);

        Button launch = actionButton(context, "LAUNCH AGE OF EMPIRES III", false);
        launch.setEnabled(imported);
        launch.setAlpha(imported ? 1.0f : 0.48f);
        launch.setOnClickListener(v -> {
            if (actions != null) actions.onLaunchGame();
        });
        LinearLayout.LayoutParams launchParams = params(-1, -2);
        launchParams.topMargin = dp(context, 8);
        gameContent.addView(launch, launchParams);
        page.addView(gameCard, params(-1, -2));

        TextView setupTitle = text(context, "SETUP & CONTROLS", 12, 0xFF8EA5C8, true);
        LinearLayout.LayoutParams sectionParams = params(-1, -2);
        sectionParams.topMargin = dp(context, 24);
        page.addView(setupTitle, sectionParams);

        page.addView(actionCard(context, "Containers",
            "Create and manage isolated Windows environments",
            "MANAGE CONTAINERS", v -> { if (actions != null) actions.onOpenContainers(); }),
            params(-1, -2));

        page.addView(actionCard(context, "Graphics",
            "Configure the existing driver and DirectX translation options",
            "GRAPHICS SETTINGS", v -> { if (actions != null) actions.onOpenGraphicsSettings(); }),
            params(-1, -2));

        page.addView(actionCard(context, "Controls",
            "Configure controller bindings and touchscreen input",
            "INPUT SETTINGS", v -> { if (actions != null) actions.onOpenInputSettings(); }),
            params(-1, -2));

        TextView footer = text(context,
            imported
                ? "The game files are imported and a 64-bit Windows executable was detected. Compatibility and performance are not yet verified."
                : "The launcher unlocks after importing the game folder and detecting a 64-bit Windows executable.",
            12, 0xFF8995A8, false);
        LinearLayout.LayoutParams footerParams = params(-1, -2);
        footerParams.topMargin = dp(context, 18);
        page.addView(footer, footerParams);

        return scroll;
    }

    private static LinearLayout actionCard(Context context, String title, String description,
                                           String buttonText, View.OnClickListener listener) {
        LinearLayout card = card(context);
        LinearLayout content = column(context, dp(context, 14));
        content.addView(text(context, title, 18, 0xFFFFFFFF, true));
        content.addView(text(context, description, 13, 0xFFB7C1D1, false));
        Button button = actionButton(context, buttonText, false);
        button.setOnClickListener(listener);
        content.addView(button);
        card.addView(content);
        LinearLayout.LayoutParams lp = params(-1, -2);
        lp.topMargin = dp(context, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private static LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(background(0xFF202A39, 0xFF34445B, dp(context, 18), dp(context, 1)));
        card.setElevation(dp(context, 2));
        LinearLayout.LayoutParams lp = params(-1, -2);
        lp.topMargin = dp(context, 18);
        card.setLayoutParams(lp);
        return card;
    }

    private static Button actionButton(Context context, String label, boolean primary) {
        Button button = new Button(context);
        button.setText(label);
        button.setTextSize(13);
        button.setTextColor(0xFFFFFFFF);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setMinHeight(dp(context, 48));
        button.setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8));
        button.setBackground(background(primary ? 0xFF1769AA : 0xFF34445B,
            primary ? 0xFF3987C8 : 0xFF52647A, dp(context, 10), dp(context, 1)));
        return button;
    }

    private static GradientDrawable background(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        drawable.setStroke(strokeWidth, stroke);
        return drawable;
    }

    private static LinearLayout column(Context context, int padding) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);
        return layout;
    }

    private static TextView text(Context context, String value, int sp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private static LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
