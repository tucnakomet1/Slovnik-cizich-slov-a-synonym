package com.main.abz;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod; // Důležitý import pro klikatelné odkazy
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.main.abz.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Links for GitHub
        String repoLink = "Repozitář aplikace: <a href='https://github.com/tucnakomet1/Slovnik-cizich-slov-a-synonym'>GitHub</a>";
        String profileLink = "Profil vývojáře: <a href='https://github.com/tucnakomet1/'>Tucnakomet1</a>";
        String issuesLink = "Nahlášení chyb a návrhy: <a href='https://github.com/tucnakomet1/Slovnik-cizich-slov-a-synonym/issues'>GitHub Issues</a>";
        String sourcesLink = "Zdroje dat: ABZ <a href='https://www.slovnik-synonym.cz/'>slovník synonym</a> a <a href='https://slovnik-cizich-slov.abz.cz/'>slovník cizích slov</a>.";

        binding.linkRepoTextView.setText(Html.fromHtml(repoLink, Html.FROM_HTML_MODE_LEGACY));
        binding.linkProfileTextView.setText(Html.fromHtml(profileLink, Html.FROM_HTML_MODE_LEGACY));
        binding.linkIssuesTextView.setText(Html.fromHtml(issuesLink, Html.FROM_HTML_MODE_LEGACY));
        binding.linkSourcesTextView.setText(Html.fromHtml(sourcesLink, Html.FROM_HTML_MODE_LEGACY));

        // Clickable links
        binding.linkRepoTextView.setMovementMethod(LinkMovementMethod.getInstance());
        binding.linkProfileTextView.setMovementMethod(LinkMovementMethod.getInstance());
        binding.linkIssuesTextView.setMovementMethod(LinkMovementMethod.getInstance());
        binding.linkSourcesTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Button "Back"
        binding.buttonSecond.setOnClickListener(view1 -> NavHostFragment.findNavController(SecondFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}