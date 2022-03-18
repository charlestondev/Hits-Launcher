package com.hits.hitslauncher;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class ItemLauncherAdapter extends RecyclerView.Adapter<ItemLauncherAdapter.ViewHolder>{
    public List<TileLauncher> mDataset;
    private RecyclerView.Adapter mAdapter;
    private DisplayMetrics displayMetrics;
    private Context context;
    PackageManager manager;
    public int parentHeight = 0;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        //public LinearLayout mItem;
        public ConstraintLayout mItem;
        public TextView label;
        public TextView touch_count;
        public ImageView icon_app;
        public ImageView icon_remove;
        public ViewHolder(ConstraintLayout v) {
            super(v);
            mItem = v;
            label = (TextView)v.findViewById(R.id.label);
            touch_count = (TextView)v.findViewById(R.id.touch_count);
            icon_app = (ImageView)v.findViewById(R.id.icon_app);
            icon_remove = (ImageView)v.findViewById(R.id.icon_remove);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ItemLauncherAdapter(List<TileLauncher> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public int getItemViewType(int position) {
        // here your custom logic to choose the view type
        if(position==0)
            return 3;
        return mDataset.get(position) instanceof AppTileLauncher ? 1 : 2;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public ItemLauncherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        displayMetrics = parent.getResources().getDisplayMetrics();
        context = parent.getContext();
        parentHeight = parent.getHeight();
        manager = context.getPackageManager();
        // create a new view
        View v;
        // set the view's size, margins, paddings and layout parameters

        if(viewType == 1){
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tile_launcher, parent, false);
        }else if(viewType == 2)
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.letter_button_item_launcher, parent, false);
        else{
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.widget_area, parent, false);
        }


        //ViewHolder vh = new ViewHolder((LinearLayout)v);
        ViewHolder vh = new ViewHolder((ConstraintLayout) v);
        Log.d("create view", "create");
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        int tileWidth;
        if(position<11){
            tileWidth = displayMetrics.widthPixels/5;
        }else if(position<23){
            tileWidth = displayMetrics.widthPixels/6;
        }else
            tileWidth = displayMetrics.widthPixels/7;
        if(position!=0){
            holder.mItem.getLayoutParams().height = tileWidth;
            holder.mItem.getLayoutParams().width = tileWidth;}
        else{
            holder.mItem.getLayoutParams().height = parentHeight-(tileWidth * (Launcher.linhas_recentes/5));
            holder.mItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                    context.startActivity(Intent.createChooser(intent, "Select Wallpaper"));
                    return true;
                }
            });
        }

        if(position!=0){

            if(mDataset.get(position) instanceof AppTileLauncher){
                if(((AppTileLauncher) mDataset.get(position)).fitered)
                    holder.mItem.setBackgroundColor(Color.parseColor(ConfigActivity.filteredColor));
                else
                    holder.mItem.setBackgroundColor(Color.parseColor(ConfigActivity.tilesBGColor));
                if(ConfigActivity.showAppName(context)){
                    holder.label.setVisibility(View.VISIBLE);
                    holder.label.setText(mDataset.get(position).label.substring(0,3));
                }
                if(ConfigActivity.showTouchCount(context)){
                    holder.touch_count.setVisibility(View.VISIBLE);
                    holder.touch_count.setText(((AppTileLauncher) mDataset.get(position)).touchCount+"");
                }
                holder.icon_app.setImageDrawable(mDataset.get(position).icon);



                holder.mItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AnimatorSet touchAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.touch_item_launcher);
                        touchAnimation.setTarget(holder.mItem);
                        touchAnimation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                if(!((AppTileLauncher) mDataset.get(position)).readyToRemove){
                                    SharedPreferences sharedPref = ((Launcher)context).getPreferences(Context.MODE_PRIVATE);
                                    String nome = ((AppTileLauncher)mDataset.get(position)).name;
                                    int count = sharedPref.getInt(nome+"count",0);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt(nome+"count", count+1);
                                    ((AppTileLauncher) mDataset.get(position)).touchCount = count+1;
                                    editor.commit();
                                    Intent i = manager.getLaunchIntentForPackage(((AppTileLauncher)mDataset.get(position)).name.toString());
                                    context.startActivity(i);
                                }else{
                                    Uri packageUri = Uri.parse("package:"+((AppTileLauncher) mDataset.get(position)).name);
                                    Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                                    context.startActivity(uninstallIntent);
                                }


                            }
                        });
                        touchAnimation.start();
                    }
                });
                holder.mItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        final AnimatorSet holdAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.hold_item_launcher);
                        final AnimatorSet holdAnimation2 = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.hold_item_launcher2);
                        holdAnimation.setTarget(holder.mItem);
                        holdAnimation2.setTarget(holder.mItem);
                        holdAnimation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                boolean readyToRemove = ((AppTileLauncher) mDataset.get(position)).readyToRemove;
                                ((AppTileLauncher) mDataset.get(position)).readyToRemove = !readyToRemove;
                                if(readyToRemove){
                                    holder.icon_remove.setVisibility(View.GONE);
                                    holder.icon_app.setVisibility(View.VISIBLE);
                                    holder.icon_app.setScaleX(-1);
                                }
                                else{
                                    holder.icon_remove.setVisibility(View.VISIBLE);
                                    holder.icon_app.setVisibility(View.GONE);
                                }
                                //holder.mItem.setBackgroundColor(Color.parseColor("#FF4081"));
                                holdAnimation2.start();
                            }
                        });
                        holdAnimation.start();
                        return true;
                    }
                });

                if(((AppTileLauncher) mDataset.get(position)).readyToRemove){
                    holder.icon_remove.setVisibility(View.VISIBLE);
                    holder.icon_app.setVisibility(View.GONE);
                }
                else{
                    holder.icon_remove.setVisibility(View.GONE);
                    holder.icon_app.setVisibility(View.VISIBLE);
                }
            }else{
                holder.label.setText(mDataset.get(position).label);
                if(((CharButtonTileLauncher)mDataset.get(position)).active)
                    holder.mItem.setBackgroundColor(Color.parseColor("#FF4081"));
                    //holder.mItem.setBackgroundColor(Color.parseColor("#FF00CCFF"));
                else
                    holder.mItem.setBackgroundColor(Color.parseColor("#3F51B5"));

                holder.mItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AnimatorSet touchItemLauncher = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.touch_item_launcher);
                        touchItemLauncher.setTarget(holder.mItem);

                        touchItemLauncher.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);

                                boolean active = ((CharButtonTileLauncher)mDataset.get(position)).active;
                                for(int j = 23; j < 30; j++){
                                    if(((CharButtonTileLauncher) mDataset.get(j)).active){
                                        ((CharButtonTileLauncher) mDataset.get(j)).active = false;
                                        //notifyItemChanged(j);
                                    }
                                }
                                ((CharButtonTileLauncher)mDataset.get(position)).active = !active;
                                //notifyItemChanged(position);

                                if(!active)
                                    ((Launcher)context).filterApps(((CharButtonTileLauncher)mDataset.get(position)).label);
                                else
                                    ((Launcher)context).filterApps("");
                                notifyDataSetChanged();

                            }
                        });
                        touchItemLauncher.start();
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}