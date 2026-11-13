package com.winlator;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * First screen of the Octo86 game-focused shell.
 *
 * This fragment intentionally delegates operations to the host activity:
 * game-folder import and container creation must use Winlator's own storage
 * and ContainerManager APIs, not a second parallel runtime.
 */
public final class Aoe3HomeFragment extends Fragment {
    public interface HostActions {
        void onChooseGameFolder();
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

        TextView eyebrow = text(context, "OCTO86  /  GAME LIBRARY", 12, 0xFF8EA5C8, true);
        page.addView(eyebrow);

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

        MaterialCardView gameCard = card(context);
        LinearLayout gameContent = column(context, dp(context, 18));
        gameCard.addView(gameContent);

        TextView tag = text(context, "PC GAME PROFILE", 11, 0xFF9FB7DA, true);
        gameContent.addView(tag);

        TextView gameName = text(context, "Age of Empires III", 23, 0xFFFFFFFF, true);
        gameContent.addView(gameName);

        TextView edition = text(context, "DEFINITIVE EDITION", 12, 0xFFB7C1D1, true);
        gameContent.addView(edition);

        TextView status = text(context, "Not installed yet", 13, 0xFFFFD18A, true);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(context, 14);
        gameContent.addView(status, statusParams);

        TextView note = text(context,
            "Choose your existing game installation folder. The app will not download or include game files.",
            13, 0xFFB7C1D1, false);
        LinearLayout.LayoutParams noteParams = params(-1, -2);
        noteParams.topMargin = dp(context, 6);
        gameContent.addView(note, noteParams);

        MaterialButton choose = new MaterialButton(context);
        choose.setText("SELECT GAME FOLDER");
        choose.setOnClickListener(v -> {
            if (actions != null) actions.onChooseGameFolder();
        });
        LinearLayout.LayoutParams chooseParams = params(-1, -2);
        chooseParams.topMargin = dp(context, 16);
        gameContent.addView(choose, chooseParams);
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
            "The game launcher will become available after folder import, executable detection and a tested game profile are connected.",
            12, 0xFF8995A8, false);
        LinearLayout.LayoutParams footerParams = params(-1, -2);
        footerParams.topMargin = dp(context, 18);
        page.addView(footer, footerParams);

        return scroll;
    }

    private static MaterialCardView actionCard(Context context, String title, String description,
                                                 String buttonText, View.OnClickListener listener) {
        MaterialCardView card = card(context);
        LinearLayout content = column(context, dp(context, 14));
        TextView titleView = text(context, title, 18, 0xFFFFFFFF, true);
        content.addView(titleView);
        TextView descriptionView = text(context, description, 13, 0xFFB7C1D1, false);
        content.addView(descriptionView);
        MaterialButton button = new MaterialButton(context);
        button.setText(buttonText);
        button.setOnClickListener(listener);
        content.addView(button);
        card.addView(content);
        LinearLayout.LayoutParams lp = params(-1, -2);
        lp.topMargin = dp(context, 10);
        card.setLayoutParams(lp);
        return card;
    }

    private static MaterialCardView card(Context context) {
        MaterialCardView card = new MaterialCardView(context);
        card.setRadius(dp(context, 18));
        card.setCardBackgroundColor(0xFF202A39);
        card.setStrokeColor(0xFF34445B);
        card.setStrokeWidth(dp(context, 1));
        card.setUseCompatPadding(true);
        LinearLayout.LayoutParams lp = params(-1, -2);
        lp.topMargin = dp(context, 18);
        card.setLayoutParams(lp);
        return card;
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
