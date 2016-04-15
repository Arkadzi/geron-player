package me.arkadiy.geronplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends Fragment {


    public static final String KEY = "image";
    public ImageFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(long albumID) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putLong(KEY, albumID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        if (getArguments() != null) {
            long id = getArguments().getLong(KEY, -1);
            String uri = Utils.getArtworks(id).toString();
            ImageView imageView = (ImageView) view.findViewById(R.id.cover_art);
            view.setOnClickListener(new View.OnClickListener() {
                //                public float y;
//
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.animateHighlight();
                    }
                }
            });

            Utils.getLoader(getActivity()).displayImage(uri, imageView, Utils.getOptions(getActivity()));
        }
        return view;
    }
}
